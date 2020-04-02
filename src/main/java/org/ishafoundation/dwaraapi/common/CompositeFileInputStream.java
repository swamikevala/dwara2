package org.ishafoundation.dwaraapi.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;


public class CompositeFileInputStream extends InputStream
{
	private File mFile ;
	private List<File> mFiles ;
	private InputStream mInputStream ;
	
	
	public CompositeFileInputStream(String seqId, File file, String folderNameToBeIgnored) throws FileNotFoundException
	{
		mFile=file ;
		IOFileFilter dirFilter =   FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(folderNameToBeIgnored, null));
		mFiles=(List<File>) FileUtils.listFilesAndDirs(file, TrueFileFilter.INSTANCE, dirFilter);
		Collections.sort(mFiles);
		mInputStream=new ByteArrayInputStream(seqId.getBytes());
	}
	
	/*
	public CompositeFileInputStream(File file, String folderNameToBeIgnored) throws FileNotFoundException
	{
		mFile=file ;
		//mFiles=new ArrayList<File>(Arrays.asList(file.listFiles())) ;
		IOFileFilter dirFilter =   FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(folderNameToBeIgnored, null));
		mFiles=(List<File>) FileUtils.listFilesAndDirs(file, TrueFileFilter.INSTANCE, dirFilter);
		Collections.sort(mFiles);
		mInputStream=nextInputStream() ;
	}
	*/
	@Override
	public int read() throws IOException
	{
		int result=mInputStream==null?-1:mInputStream.read() ;
		if(result<0 && (mInputStream=nextInputStream())!=null)
			return read() ;
		else return result ;
	}
	
	protected String getRelativePath(File file)
	{
		return file.getAbsolutePath().substring(mFile.getAbsolutePath().length()) ;
	}
	
	protected InputStream nextInputStream() throws FileNotFoundException
	{
		if(!mFiles.isEmpty())
		{
			File nextFile=mFiles.remove(0) ;
			byte[] relativePathByteArr = getRelativePath(nextFile).getBytes();
			// TODO
//			Long fileSize = FileUtils.sizeOf(nextFile);
//			relativePathByteArr.
//			fileSize.byteValue();
			return new ByteArrayInputStream(relativePathByteArr);
		}
		else return null ;
	}
	
	public static void main(String args[])
	{
		if(args.length!=1)
			System.out.println("Usage: checksum directory|file");
		else
		{
			System.out.println("Checksum of: "+args[0]) ;
			DigestInputStream digestInputStream=null ;
			try {
				MessageDigest messageDigest=MessageDigest.getInstance("MD5") ;
			    digestInputStream=new DigestInputStream(new CompositeFileInputStream("12345", new File(args[0]), "dwara-ignored"), messageDigest) ;
			    while(digestInputStream.read()>=0) ;
			    
			    System.out.print("\nmd5 sum=") ;
			    for(byte b: messageDigest.digest())
			    	System.out.print(String.format("%02x",b)) ;

			    digestInputStream=new DigestInputStream(new ByteArrayInputStream("12345".getBytes()), messageDigest);
			    while(digestInputStream.read()>=0) ;
			    
			    System.out.print("\nmd5 sum=") ;
			    for(byte b: messageDigest.digest())
			    	System.out.print(String.format("%02x",b)) ;
			    
			} catch(IOException | NoSuchAlgorithmException e) {
				e.printStackTrace() ;
			} finally {
				if(digestInputStream!=null) try {digestInputStream.close();} catch (IOException e) {}
			}
		}
	}
}
