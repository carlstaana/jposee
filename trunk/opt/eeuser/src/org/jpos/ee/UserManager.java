/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.ee;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Session;
import org.jpos.iso.ISOUtil;

/**
 * @author Alejandro Revilla
 */
@SuppressWarnings("unused")
public class UserManager {
    Session session;
    String digest;

    public UserManager (DB db) {
        super ();
        this.session = db.session();
    }
    public UserManager (Session session) {
        super ();
        this.session = session;
    }
    public User getUserByNick (String nick, boolean includeDeleted)
        throws HibernateException
    {
        try {
            Criteria crit = session.createCriteria (User.class)
                .add (Restrictions.eq ("nick", nick));
            if (!includeDeleted)
                crit = crit.add (Restrictions.eq ("deleted", Boolean.FALSE));
            return (User) crit.uniqueResult();
        } catch (ObjectNotFoundException ignored) { }
        return null;
    }
    public User getUserByNick (String nick)
        throws HibernateException
    {
        return getUserByNick (nick, false);
    }
    /**
     * @param nick name.
     * @param seed initial seed
     * @param pass hash
     * @return the user
     * @throws BLException if invalid user/pass
     * @throws HibernateException on low level hibernate related exception
     */
    public User getUserByNick (String nick, String seed, String pass) 
        throws HibernateException, BLException
    {
        User u = getUserByNick (nick);
        assertNotNull (u, "User does not exist");
        assertTrue (checkPassword (u, seed, pass), "Invalid password");
        return u;
    }
    /**
     * @param u the user
     * @param seed initial seed
     * @param pass hash
     * @return true if password matches
     * @throws BLException if invalid user/pass
     * @throws HibernateException on low level hibernate related exception
     */
    public boolean checkPassword (User u, String seed, String pass) 
        throws HibernateException, BLException
    {
        assertNotNull (seed, "Invalid seed");
        assertNotNull (pass, "Invalid pass");
        String password = u.getPassword();
        assertNotNull (password, "Password is null");
        String computedPass = getHash (seed, password);
        return pass.equals (computedPass);
    }
    /**
     * @return all users
     * @throws HibernateException on low level hibernate related exception
     */
    public List findAll () throws HibernateException {
        return session.createCriteria (User.class)
                .add (Restrictions.eq ("deleted", Boolean.FALSE))
                .list();
    }

       public static String getHash (String userName, String pass) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance ("MD5");
            md.update (userName.getBytes());
            hash = ISOUtil.hexString (
                md.digest (pass.getBytes())
            ).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            // should never happen
        }
        return hash;
    }
    public static String getHash (String s) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance ("MD5");
            hash = ISOUtil.hexString (md.digest (s.getBytes())).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            // should never happen
        }
        return hash;
    }
    /*
    public static String getRandomHash () {
        return getHash (
            Double.toString (Math.random()),
            Double.toString (Math.random())
        );
    }
    */
    
    private void assertNotNull (Object obj, String error) throws BLException {
        if (obj == null)
            throw new BLException (error);
    }
    private void assertTrue (boolean condition, String error) 
        throws BLException 
    {
        if (!condition)
            throw new BLException (error);
    }
}

