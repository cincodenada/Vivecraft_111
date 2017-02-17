package com.mtbs3d.minecrift.gameplay;

import com.mtbs3d.minecrift.provider.MCOpenVR;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Created by Hendrik on 02-Aug-16.
 */
public class SwimTracker {

	Vec3d motion=Vec3d.ZERO;
	double friction=0.9f;

	double lastDist;

	final double riseSpeed=0.005f;
	double swimspeed=0.8f;

	public boolean isActive(EntityPlayerSP p){
		if(Minecraft.getMinecraft().vrSettings.seated)
			return false;
		if(!Minecraft.getMinecraft().vrSettings.realisticSwimEnabled)
			return false;
		if(p==null || p.isDead)
			return false;
		if(!p.isInWater() && !p.isInLava())
			return false;

		return true;
	}

	public void doProcess(Minecraft minecraft, EntityPlayerSP player){
		if(!isActive(player)) {
			return;
		}

		{//float
			Vec3d face = minecraft.roomScale.getHMDPos_World();
			float height = (float) (minecraft.roomScale.getHMDPos_Room().yCoord * 0.9);
			if(height > 1.6)height = 1.6f;
			Vec3d feets = face.subtract(0,height, 0);
			double waterLine=256;

			BlockPos bp = new BlockPos(feets);
			for (int i = 0; i < 4; i++) {
				Material mat=player.world.getBlockState(bp).getMaterial();
				if(!mat.isLiquid())
				{
					waterLine=bp.getY();
					break;
				}
				bp = bp.up();
			}

			double percent = (waterLine - feets.yCoord) / (face.yCoord - feets.yCoord);

			if(percent < 0){
				//how did u get here, drybones?
				return;
			}

			if(percent < 0.5 && player.onGround){
				return;
				//no diving in the kiddie pool.
			}

			player.addVelocity(0, 0.018D , 0); //counteract most gravity.

			double neutal = player.isCollidedHorizontally? 0.5 : 1;

			if(percent > neutal && percent < 2){ //between halfway submerged and 1 body length under.
				//rise!
				double buoyancy = 2 - percent;
				if(player.isCollidedHorizontally)  player.addVelocity(0, 00.03f, 0);	
				player.addVelocity(0, 0.0015 + buoyancy/100 , 0);		
			}

		}
		{//swim

			Vec3d controllerR= minecraft.roomScale.getControllerPos_World(0);
			Vec3d controllerL= minecraft.roomScale.getControllerPos_World(1);

			Vec3d middle= controllerL.subtract(controllerR).scale(0.5).add(controllerR);

			Vec3d hmdPos=minecraft.roomScale.getHMDPos_World().subtract(0,0.3,0);

			Vec3d movedir=middle.subtract(hmdPos).normalize().add(
					minecraft.roomScale.getHMDDir_World()).scale(0.5);

			Vec3d contollerDir= minecraft.roomScale.getCustomControllerVector(0,new Vec3d(0,0,-1)).add(
					minecraft.roomScale.getCustomControllerVector(1,new Vec3d(0,0,-1))).scale(0.5);
			double dirfactor=contollerDir.add(movedir).lengthVector()/2;

			double distance= hmdPos.distanceTo(middle);
			double distDelta=lastDist-distance;

			if(distDelta>0){
				Vec3d velo=movedir.scale(distDelta*swimspeed*dirfactor);
				motion=motion.add(velo.scale(0.15));
			}

			lastDist=distance;
			player.addVelocity(motion.xCoord,motion.yCoord,motion.zCoord);
			motion=motion.scale(friction);
		}

	}

}
