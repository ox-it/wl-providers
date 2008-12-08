package edu.amc.sakai.user;

import org.sakaiproject.user.api.UserEdit;

public class OakLdapAttributeMapper extends SimpleLdapAttributeMapper {
    static final String OAK_PREFIX = "oakPrimaryPrincipal="; 
    static final String KRB_PREFIX = "krbPrincipalName="; 
    static final String SUFFIX 
        = "@OX.AC.UK,cn=OX.AC.UK,cn=KerberosRealms,dc=oak,dc=ox,dc=ac,dc=uk";
    static final String OAK_EMAIL = "oakAlternativeMail"; 
    
    /** 
     * Get an LDAP filter suitable for the Oxford Oak LDAP.
     * @returns an LDAP filter suitable for the Oxford Oak LDAP.
     */
    public String getFindUserByEidFilter(String eid) {
        return OAK_PREFIX + KRB_PREFIX + eid + SUFFIX; 
    }

    public void mapUserDataOntoUserEdit( LdapUserData userData,
        UserEdit userEdit ) {
        super.mapUserDataOntoUserEdit( userData, userEdit );
        // Override the value set by the super class:
        userEdit.setEid(oakPrimaryPrincipalToLoginName(userData.getEid()));
        // Override value set in superclass:
        userEdit.setFirstName(firstName(userData));
    }
    
    private String oakPrimaryPrincipalToLoginName( String value ) {
        return value.substring( KRB_PREFIX.length(), 
            value.indexOf( SUFFIX.charAt( 0 ), KRB_PREFIX.length() ) );
    }
    
    private String firstName( LdapUserData ud ) {
        /*
         * We parse the first name from the display name by stripping out the
         * surname, shifted 1 position to the left to avoid a trailing
         * whitespace. If the display name doesn't include the surname then we 
         * just resort to givenName - middle initial(s) and all!
         */
        int i = ud.getFirstName().lastIndexOf( ud.getLastName() );
        return (i != -1) ? ud.getFirstName().substring( 0, i - 1 ) 
                        : ud.getProperties().getProperty( "givenName" );
    }

    /**
     * Overridden for the Oxford Oak LDAP. This method ensures that we search
     * e-mail using the multi-valued {@link OAK_EMAIL} attribute, which always
     * includes the "official" Oxford e-mail address.
     */
    public String getFindUserByEmailFilter( String emailAddr ) {
        return OAK_EMAIL + "=" + escapeSearchFilterTerm(emailAddr);
    }
}
