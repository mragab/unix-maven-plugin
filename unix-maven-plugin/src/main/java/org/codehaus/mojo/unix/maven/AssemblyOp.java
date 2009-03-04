package org.codehaus.mojo.unix.maven;

import static fj.Function.compose;
import static fj.Function.curry;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.FileAttributes;
import static org.codehaus.mojo.unix.FileAttributes.useAsDefaultsFor;
import org.codehaus.mojo.unix.core.AssemblyOperation;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AssemblyOp
{
    private final Map<String, Artifact> mutableArtifactMap = new HashMap<String, Artifact>();

    protected final Map<String, Artifact> artifactMap = Collections.unmodifiableMap( mutableArtifactMap );

    protected final String operationType;

    protected AssemblyOp( String operationType )
    {
        this.operationType = operationType;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public abstract AssemblyOperation createOperation( FileObject basedir, Defaults defaults )
        throws MojoFailureException, FileSystemException;

    // -----------------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------------

    public void setArtifactMap( Map<String, Artifact> artifactMap )
    {
        mutableArtifactMap.putAll( artifactMap );
    }

    protected FileObject resolve( FileSystem basedir, File file )
        throws FileSystemException
    {
        return basedir.resolveFile( file.getAbsolutePath() );
    }

    public static FileAttributes applyFileDefaults( Defaults defaults, FileAttributes attributes )
    {
        return compose( curry( useAsDefaultsFor, Defaults.DEFAULT_FILE_ATTRIBUTES ),
                        curry( useAsDefaultsFor, defaults.getFileAttributes() ) ).
            f( attributes );
    }

    public static FileAttributes applyDirectoryDefaults( Defaults defaults, FileAttributes attributes )
    {
        return compose( curry( useAsDefaultsFor, Defaults.DEFAULT_DIRECTORY_ATTRIBUTES ),
                        curry( useAsDefaultsFor, defaults.getDirectoryAttributes() ) ).
            f( attributes );
    }

    protected static String nullifEmpty( String artifact )
    {
        return StringUtils.clean( artifact ).length() == 0 ? null : artifact;
    }

    protected void validateIsSet( Object valueA, String fieldA )
        throws MojoFailureException
    {
        if ( valueA == null )
        {
            throw new MojoFailureException( "Field '" + fieldA + "' has to be specified on a " + operationType + " operation." );
        }
    }

    protected void validateEitherIsSet( Object valueA, Object valueB, String fieldA, String fieldB )
        throws MojoFailureException
    {
        if ( valueA != null && valueB != null )
        {
            throw new MojoFailureException( "Only one of '" + fieldA + "' or '" + fieldB + "' can be specified on a " + operationType + " operation." );
        }

        if ( valueA == null && valueB == null )
        {
            throw new MojoFailureException( "Either '" + fieldA + "' or '" + fieldB + "' has to be specified on a " + operationType + " operation." );
        }
    }

    protected RelativePath validateAndResolveOutputFile( File artifactFile, RelativePath toDir, RelativePath toFile )
        throws MojoFailureException
    {
        if ( toFile != null )
        {
            if ( toDir != null )
            {
                throw new MojoFailureException( "Can't specify both 'toDir' and 'toFile' on a " + operationType + " operation." );
            }

            return toFile;
        }

        if ( toDir == null )
        {
            toDir = RelativePath.BASE;
        }

        return toDir.add( artifactFile.getName() );
    }

    protected File validateFileIsReadableFile( File file, String fieldName )
        throws MojoFailureException
    {
        if ( !file.isFile() )
        {
            throw new MojoFailureException( "The path specified in field '" + fieldName + "' on an " + operationType +
                " operation is not a file: " + file.getAbsolutePath() );
        }

        if ( !file.canRead() )
        {
            throw new MojoFailureException( "The path specified in field '" + fieldName + " on an " +
                operationType + " operation is not readable " + file.getAbsolutePath() );
        }

        return file;
    }

    protected File validateFileIsDirectory( File file, String fieldName )
        throws MojoFailureException
    {
        if ( !file.isDirectory() )
        {
            throw new MojoFailureException( "The path specified in field '" + fieldName + "' on an " + operationType +
                " is not a directory: " + file.getAbsolutePath() );
        }

        return file;
    }

    protected File validateArtifact( String artifact )
        throws MojoFailureException
    {
        Artifact a = artifactMap.get( artifact );

        if ( a != null )
        {
            return a.getFile();
        }

        a = artifactMap.get( artifact + ":jar" );

        if ( a != null )
        {
            return a.getFile();
        }

        Map map = new TreeMap<String, Artifact>( artifactMap );

        // TODO: Do not log here, throw a CouldNotFindArtifactException with the map as an argument
        System.out.println("Could not find artifact:" + artifact );
        System.out.println("Available artifacts:");
        for ( Object o : map.keySet() )
        {
            System.out.println( o );
        }

        throw new MojoFailureException( "Could not find artifact '" + artifact + "'." );
    }
}
