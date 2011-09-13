package edu.amc.sakai.user;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.GroupProvider;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

public class JLDAPGroupProvider implements GroupProvider {

	private static final Log log = LogFactory.getLog(JLDAPDirectoryProvider.class);

	private LdapConnectionManager connectionManager;
	private ProvidedGroupManager groupManager;
	private JLDAPDirectoryProvider jldapDirectoryProvider;
	
	public void init() {
		// Want to share the connection manager.
		setConnectionManager(jldapDirectoryProvider.getLdapConnectionManager());
		log.warn("Create group: "+ createGroup("oakUnitCode=oucs,ou=units,dc=oak,dc=ox,dc=ac,dc=uk", "access"));
	}
	
	public ProvidedGroup createGroup(String dn, String role) {
		return groupManager.newGroup(dn, role);
	}
	
	public boolean groupExists(String groupId) {
		ProvidedGroup group = groupManager.getGroup(groupId);
		return group != null;
	}
	
	public Map getGroupRolesForUser(String eid) {
		// Do subtree search on LDAP, then map to groups using the group manager.
		String personId = MessageFormat.format("oakPrimaryPersonID={0},ou=people,dc=oak,dc=ox,dc=ac,dc=uk", eid);
		String filter = "member="+personId;
		try {
			LDAPConnection connection = connectionManager.getConnection();
			LDAPSearchResults results = connection.search("ou=units,dc=oak,dc=ox,dc=ac,dc=uk", LDAPConnection.SCOPE_SUB, filter, new String[]{"dn"}, false);
			HashMap<String, String> groupRoles = new HashMap<String, String>();
			while (results.hasMore()) {
				LDAPEntry resultEntry = results.next();
				String dn = resultEntry.getDN();
				Set<ProvidedGroup> groups = groupManager.getGroupByDNs(dn);
				if (groups != null) {
					for (ProvidedGroup group: groups) {
						groupRoles.put(group.getId(), group.getRole());
					}
				}
			}
			return groupRoles;
		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getRole(String groupId, String eid) {
		ProvidedGroup group = groupManager.getGroup(groupId);
		if (group != null) {
			return group.getRole();
		}
		return null;
	}

	public Map getUserRolesForGroup(String groupId) {
		//TODO Need to unpack the groupId
		ProvidedGroup group = groupManager.getGroup(groupId);
		if (group != null) {
			String dn = group.getDn();
			LDAPConnection connection;
			try {
				connection = connectionManager.getConnection();
				LDAPSearchResults results = connection.search(dn, LDAPConnection.SCOPE_ONE, null, new String[]{"member"}, false);
				Map userRoles = new HashMap();;
				while (results.hasMore()) {
					LDAPEntry result = results.next();
					LDAPAttribute member = result.getAttribute("member");
					Enumeration values = member.getStringValues();
					while (values.hasMoreElements()) {
						String value = (String) values.nextElement();
						MessageFormat message = new MessageFormat("oakPrimaryPersonID={0},ou=people,dc=oak,dc=ox,dc=ac,dc=uk");
						try {
							Object[] personIds = message.parse(value);
							if (personIds.length == 1) {
								userRoles.put(personIds[0], group.getRole());
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
				return userRoles;
			} catch (LDAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Collections.EMPTY_MAP;
	}

	public String packId(String[] ids) {
		return ids[0];
	}

	public String preferredRole(String one, String other) {
		return one;
	}

	public String[] unpackId(String id) {
		return new String[]{id};
	}

	public void setConnectionManager(LdapConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public LdapConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public JLDAPDirectoryProvider getJldapDirectoryProvider() {
		return jldapDirectoryProvider;
	}

	public void setJldapDirectoryProvider(
			JLDAPDirectoryProvider jldapDirectoryProvider) {
		this.jldapDirectoryProvider = jldapDirectoryProvider;
	}

	public ProvidedGroupManager getGroupManager() {
		return groupManager;
	}

	public void setGroupManager(ProvidedGroupManager groupManager) {
		this.groupManager = groupManager;
	}

}
