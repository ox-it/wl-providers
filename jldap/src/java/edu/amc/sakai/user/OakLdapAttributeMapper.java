package edu.amc.sakai.user;

import org.sakaiproject.user.api.UserEdit;

public class OakLdapAttributeMapper extends MultipleEmailLdapAttributeMapper {

    public void mapUserDataOntoUserEdit( LdapUserData userData,
        UserEdit userEdit ) {
        super.mapUserDataOntoUserEdit( userData, userEdit );
        // Override value set in superclass:
        userEdit.setFirstName(firstName(userData));
        String displayId = userEdit.getProperties().getProperty(JLDAPDirectoryProvider.DISPLAY_ID_PROPERTY);
        if (displayId == null || displayId.length() == 0) {
        	userEdit.getProperties().addProperty(JLDAPDirectoryProvider.DISPLAY_ID_PROPERTY, "none");
        }
    }

    private String firstName( LdapUserData ud ) {
        int i = (ud.getFirstName()!= null)?ud.getFirstName().lastIndexOf( " "+ud.getLastName() ):-1;
        return (i > 0) ? ud.getFirstName().substring( 0, i ) 
                        : ud.getProperties().getProperty( "givenName" );
    }

}
