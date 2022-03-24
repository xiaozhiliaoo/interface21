/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.multipart.commons;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.fileupload.DefaultFileItem;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.multipart.MultipartFile;

/**
 * MultipartFile implementation for Jakarta Commons FileUpload.
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @since 29.09.2003
 * @see CommonsMultipartResolver
 */
public class CommonsMultipartFile implements MultipartFile, Serializable {

	protected static final Log logger = LogFactory.getLog(CommonsMultipartFile.class);

	private final FileItem fileItem;

	private final long size;

	/**
	 * Create an instance wrapping the given FileItem.
	 * @param fileItem the FileItem to wrap
	 */
	protected CommonsMultipartFile(FileItem fileItem) {
		this.fileItem = fileItem;
		this.size = this.fileItem.getSize();
	}

	/**
	 * Return the underlying <code>org.apache.commons.fileupload.FileItem</code>
	 * instance. There is hardly any need to access this.
	 */
	public FileItem getFileItem() {
		return fileItem;
	}

	public String getName() {
		return this.fileItem.getFieldName();
	}

	public boolean isEmpty() {
		return (this.size == 0);
	}

	public String getOriginalFilename() {
		if (this.fileItem.getName() == null) {
			return null;
		}
		// check for Unix-style path
		int pos = this.fileItem.getName().lastIndexOf("/");
		if (pos == -1) {
			// check for Windows-style path
			pos = this.fileItem.getName().lastIndexOf("\\");
		}
		if (pos != -1)  {
			// any sort of path separator found
			return this.fileItem.getName().substring(pos + 1);
		}
		else {
			// plain name
			return this.fileItem.getName();
		}
	}

	public String getContentType() {
		return this.fileItem.getContentType();
	}

	public long getSize() {
		return size;
	}

	public byte[] getBytes() {
		if (!isAvailable()) {
			throw new IllegalStateException("File has been moved - cannot be read again");
		}
		byte[] bytes = this.fileItem.get();
		return (bytes != null ? bytes : new byte[0]);
	}

	public InputStream getInputStream() throws IOException {
		if (!isAvailable()) {
			throw new IllegalStateException("File has been moved - cannot be read again");
		}
		InputStream inputStream = this.fileItem.getInputStream();
		return (inputStream != null ? inputStream : new ByteArrayInputStream(new byte[0]));
	}

	public void transferTo(File dest) throws IOException, IllegalStateException {
		if (!isAvailable()) {
			throw new IllegalStateException("File has already been moved - cannot be transferred again");
		}

		if (dest.exists() && !dest.delete()) {
			throw new IOException(
					"Destination file [" + dest.getAbsolutePath() + "] already exists and could not be deleted");
		}

		try {
			this.fileItem.write(dest);
			if (logger.isDebugEnabled()) {
				String action = "transferred";
				if (!this.fileItem.isInMemory()) {
					action = isAvailable() ? "copied" : "moved";
				}
				logger.debug("Multipart file '" + getName() + "' with original filename [" +
						getOriginalFilename() + "], stored " + getStorageDescription() + ": " +
						action + " to [" + dest.getAbsolutePath() + "]");
			}
		}
		catch (FileUploadException ex) {
			throw new IllegalStateException(ex.getMessage());
		}
		catch (IOException ex) {
			throw ex;
		}
		catch (Exception ex) {
			logger.error("Could not transfer to file", ex);
			throw new IOException("Could not transfer to file: " + ex.getMessage());
		}
	}

	/**
	 * Determine whether the multipart content is still available.
	 * If a temporary file has been moved, the content is no longer available.
	 */
	protected boolean isAvailable() {
		// If in memory, it's available.
		if (this.fileItem.isInMemory()) {
			return true;
		}
		// Check actual existence of temporary file.
		if (this.fileItem instanceof DefaultFileItem) {
			return ((DefaultFileItem) this.fileItem).getStoreLocation().exists();
		}
		// Check whether current file size is different than original one.
		return (this.fileItem.getSize() == this.size);
	}

	/**
	 * Return a description for the storage location of the multipart content.
	 * Tries to be as specific as possible: mentions the file location in case
	 * of a temporary file.
	 */
	protected String getStorageDescription() {
		if (this.fileItem.isInMemory()) {
			return "in memory";
		}
		else if (this.fileItem instanceof DefaultFileItem) {
			return "at [" + ((DefaultFileItem) this.fileItem).getStoreLocation().getAbsolutePath() + "]";
		}
		else {
			return "on disk";
		}
	}

}
