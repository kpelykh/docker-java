package com.kpelykh.docker.client.utils;

import java.util.Arrays;


/**
 * Encapsulating unix file permissions.
 *
 * @version $Id$
 */
public class Permission {

    private String mask;

    // {read, write, execute}
    private boolean[] owner = new boolean[3];
    private boolean[] group = new boolean[3];
    private boolean[] other = new boolean[3];

    /**
     * @param mask the access string to parse the permissions from.
     *          Must be someting like -rwxrwxrwx
     */
    public Permission(String mask) {
        this.mask = mask;
        this.owner = this.getOwnerPermissions(mask);
        this.group = this.getGroupPermissions(mask);
        this.other = this.getOtherPermissions(mask);
//		log.debug("Permission:"+this.toString());
    }

    /**
     * @return The unix access permissions including the the first bit
     */
    public String getMask() {
        return this.mask;
    }

    /**
     * @return a thee-dimensional boolean array representing read, write
     *         and execute permissions (in that order) of the file owner.
     */
    public boolean[] getOwnerPermissions() {
        return owner;
    }

    /**
     * @return a thee-dimensional boolean array representing read, write
     *         and execute permissions (in that order) of the group
     */
    public boolean[] getGroupPermissions() {
        return group;
    }

    /**
     * @return a thee-dimensional boolean array representing read, write
     *         and execute permissions (in that order) of any user
     */
    public boolean[] getOtherPermissions() {
        return other;
    }

    private boolean[] getOwnerPermissions(String s) {
        boolean[] b = {
                s.charAt(1) == 'r',
                s.charAt(2) == 'w',
                s.charAt(3) == 'x' || s.charAt(3) == 's' || s.charAt(3) == 'S' || s.charAt(3) == 't' || s.charAt(3) == 'T' || s.charAt(3) == 'L'};
        return b;
    }

    private boolean[] getGroupPermissions(String s) {
        boolean[] b = {
                s.charAt(4) == 'r',
                s.charAt(5) == 'w',
                s.charAt(6) == 'x' || s.charAt(6) == 's' || s.charAt(6) == 'S' || s.charAt(6) == 't' || s.charAt(6) == 'T' || s.charAt(6) == 'L'};
        return b;
    }

    private boolean[] getOtherPermissions(String s) {
        boolean[] b = {
                s.charAt(7) == 'r',
                s.charAt(8) == 'w',
                s.charAt(9) == 'x' || s.charAt(9) == 's' || s.charAt(9) == 'S' || s.charAt(9) == 't' || s.charAt(9) == 'T' || s.charAt(9) == 'L'};
        return b;
    }

    /**
     * @return i.e. rwxrwxrwx (777)
     */
    public String toString() {
        return this.getMask() + " (" + this.getOctalCode() + ")";
    }

    /**
     * @return The unix equivalent octal access code like 777
     */
    public int getOctalCode() {
        String owner = "" + this.getOctalAccessNumber(this.getOwnerPermissions());
        String group = "" + this.getOctalAccessNumber(this.getGroupPermissions());
        String other = "" + this.getOctalAccessNumber(this.getOtherPermissions());
        return Integer.parseInt(owner + group + other);
    }

    /*
    *	0 = no permissions whatsoever; this person cannot read, write, or execute the file
     *	1 = execute only
     *	2 = write only
     *	3 = write and execute (1+2)
     *	4 = read only
     *	5 = read and execute (4+1)
     *	6 = read and write (4+2)
     *	7 = read and write and execute (4+2+1)
     */

    //-rwxrwxrwx

    private int getOctalAccessNumber(boolean[] permissions) {
        if (Arrays.equals(permissions, new boolean[]{false, false, false})) {
            return 0;
        }
        if (Arrays.equals(permissions, new boolean[]{false, false, true})) {
            return 1;
        }
        if (Arrays.equals(permissions, new boolean[]{false, true, false})) {
            return 2;
        }
        if (Arrays.equals(permissions, new boolean[]{false, true, true})) {
            return 3;
        }
        if (Arrays.equals(permissions, new boolean[]{true, false, false})) {
            return 4;
        }
        if (Arrays.equals(permissions, new boolean[]{true, false, true})) {
            return 5;
        }
        if (Arrays.equals(permissions, new boolean[]{true, true, false})) {
            return 6;
        }
        if (Arrays.equals(permissions, new boolean[]{true, true, true})) {
            return 7;
        }
        return -1;
    }
}