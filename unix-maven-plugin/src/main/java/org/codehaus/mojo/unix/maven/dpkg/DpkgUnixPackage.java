package org.codehaus.mojo.unix.maven.dpkg;

import fj.F;
import fj.data.Option;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.UnixFsObject;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.dpkg.Dpkg;
import org.codehaus.mojo.unix.maven.FsFileCollector;
import org.codehaus.mojo.unix.maven.ScriptUtil;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.mojo.unix.util.UnixUtil;
import static org.codehaus.mojo.unix.util.vfs.VfsUtil.asFile;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgUnixPackage
    extends UnixPackage
{
    private final ControlFile controlFile = new ControlFile();

    private FsFileCollector fileCollector;

    private String dpkgDebPath;

    private boolean debug;

    private final static ScriptUtil scriptUtil;

    static
    {
        scriptUtil = new ScriptUtil.ScriptUtilBuilder().
            format( "dpkg" ).
            setPreInstall( "preinst" ).
            setPostInstall( "postinst" ).
            setPreRemove( "prerm" ).
            setPostRemove( "postrm" ).
            build();
    }

    public DpkgUnixPackage()
    {
        super( "deb" );
    }

    public UnixPackage mavenCoordinates( String groupId, String artifactId, String classifier )
    {
        controlFile.groupId = groupId;
        controlFile.artifactId = artifactId;

        return this;
    }

//    public UnixPackage dependencies( Set<DebianDependency> dependencies )
//    {
//        controlFile.dependencies = dependencies;
//        return this;
//    }

    public UnixPackage name( String name )
    {
        controlFile._package = name;
        return this;
    }

    public UnixPackage shortDescription( String shortDescription )
    {
        controlFile.shortDescription = shortDescription;
        return this;
    }

    public UnixPackage description( String description )
    {
        controlFile.description = description;
        return this;
    }

    public UnixPackage contact( String contact )
    {
        controlFile.maintainer = contact;
        return this;
    }

    public UnixPackage architecture( String architecture )
    {
        controlFile.architecture = architecture;
        return this;
    }

    public UnixPackage workingDirectory( FileObject workingDirectory )
        throws FileSystemException
    {
        fileCollector = new FsFileCollector( workingDirectory.resolveFile( "assembly" ) );
        return this;
    }

    public UnixPackage debug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public FileObject getRoot()
    {
        return fileCollector.getRoot();
    }

    public FileCollector addDirectory( UnixFsObject.Directory directory )
    {
        fileCollector.addDirectory( directory );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, UnixFsObject.RegularFile file )
    {
        fileCollector.addFile( fromFile, file );

        return this;
    }

    public FileCollector addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        fileCollector.addSymlink( symlink );

        return this;
    }

    public void applyOnFiles( F<RelativePath, Option<FileAttributes>> f )
    {
        fileCollector.applyOnFiles( f );
    }

    public void applyOnDirectories( F<RelativePath, Option<FileAttributes>> f )
    {
        fileCollector.applyOnDirectories( f );
    }

    // -----------------------------------------------------------------------
    // Debian Specifics
    // -----------------------------------------------------------------------

    public DpkgUnixPackage dpkgDeb( String dpkgDeb )
    {
        this.dpkgDebPath = dpkgDeb;
        return this;
    }

    public DpkgUnixPackage section( String secion )
    {
        controlFile.section = secion;
        return this;
    }

    public DpkgUnixPackage priority( String priority )
    {
        controlFile.priority = priority;
        return this;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void packageToFile( File packageFile )
        throws Exception
    {
        File assembly = asFile( fileCollector.getFsRoot() );
        controlFile.version = getVersion();
        controlFile.toFile( assembly );

        fileCollector.collect();

        ScriptUtil.Execution execution = scriptUtil.copyScripts( getBasedir(), new File( assembly, "DEBIAN" ) );

        UnixUtil.chmodIf( execution.hasPreInstall(), execution.getPreInstall(), "0755" );
        UnixUtil.chmodIf( execution.hasPostInstall(), execution.getPostInstall(), "0755" );
        UnixUtil.chmodIf( execution.hasPreRemove(), execution.getPreRemove(), "0755" );
        UnixUtil.chmodIf( execution.hasPostRemove(), execution.getPostRemove(), "0755" );

        new Dpkg().
            setDebug( debug ).
            setPackageRoot( assembly ).
            setDebFile( packageFile ).
            setDpkgDebPath( dpkgDebPath ).
            execute();
    }

    public static DpkgUnixPackage cast( UnixPackage unixPackage )
    {
        return (DpkgUnixPackage) unixPackage;
    }
}
