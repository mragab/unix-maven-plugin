package org.codehaus.mojo.unix.maven;

import org.codehaus.mojo.unix.UnixFileMode;
import static fj.data.Option.fromNull;
import fj.F;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FileAttributes
{
    public String user;

    public String group;

    public UnixFileMode mode = null;

    public FileAttributes()
    {
    }

    // Package protected for Defaults
    FileAttributes( String user, String group, UnixFileMode mode )
    {
        this.user = user;
        this.group = group;
        this.mode = mode;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser( String user )
    {
        this.user = user;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup( String group )
    {
        this.group = group;
    }

    public UnixFileMode getMode()
    {
        return mode;
    }

    public void setMode( String mode )
    {
        this.mode = UnixFileMode.fromInt( Integer.parseInt( mode, 8 ) );
    }

    public org.codehaus.mojo.unix.FileAttributes create()
    {
        return new org.codehaus.mojo.unix.FileAttributes( fromNull( user ), fromNull( group ), fromNull( mode ) );
    }

    public static final F<FileAttributes, org.codehaus.mojo.unix.FileAttributes> create_ = new F<FileAttributes, org.codehaus.mojo.unix.FileAttributes>()
    {
        public org.codehaus.mojo.unix.FileAttributes f( FileAttributes fileAttributes )
        {
            return fileAttributes.create();
        }
    };
}
