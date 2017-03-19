package com.mtbs3d.minecrift.render;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import shadersmod.client.ShadersTex;

public class StaticTexture extends SimpleTexture{
	
	private static final Logger LOG = LogManager.getLogger();
	
	public StaticTexture(ResourceLocation textureResourceLocation) {
		super(textureResourceLocation);
	}
	
	  @Override
	  public void loadTexture(IResourceManager resourceManager) throws IOException
	    {
	        this.deleteGlTexture();
	        IResource iresource = null;

	        try
	        {
	            BufferedImage bufferedimage = TextureUtil.readBufferedImage(StaticTexture.class.getResourceAsStream(this.textureLocation.getResourcePath()));
	            boolean flag = false;
	            boolean flag1 = false;

	            if (Config.isShaders())
	            {
	                ShadersTex.loadSimpleTexture(this.getGlTextureId(), bufferedimage, flag, flag1, resourceManager, this.textureLocation, this.getMultiTexID());
	            }
	            else
	            {
	                TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), bufferedimage, flag, flag1);
	            }
	        }
	        finally
	        {
	            IOUtils.closeQuietly((Closeable)iresource);
	        }
	    }
}
