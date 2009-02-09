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

}
