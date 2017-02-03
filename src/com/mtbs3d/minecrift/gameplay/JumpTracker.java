package com.mtbs3d.minecrift.gameplay;

import com.mojang.realmsclient.dto.RealmsServer.McoServerComparator;
import com.mtbs3d.minecrift.api.IRoomscaleAdapter;
import com.mtbs3d.minecrift.api.NetworkHelper;
import com.mtbs3d.minecrift.provider.MCOpenVR;

import com.mtbs3d.minecrift.settings.AutoCalibration;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class JumpTracker {

	public Vec3d[] latchStart = new Vec3d[]{new Vec3d(0,0,0), new Vec3d(0,0,0)};
	public Vec3d[] latchStartOrigin = new Vec3d[]{new Vec3d(0,0,0), new Vec3d(0,0,0)};
	public Vec3d[] latchStartPlayer = new Vec3d[]{new Vec3d(0,0,0), new Vec3d(0,0,0)};
	private boolean c0Latched = false;
	private boolean c1Latched = false;
	
	private Minecraft mc;

    public boolean isClimbeyJump(){
    	if(!this.isActive(mc.player)) return false;
    	return(isClimbeyJumpEquipped());
    }
    
    public boolean isClimbeyJumpEquipped(){
    	return(NetworkHelper.serverAllowsClimbey && mc.player.isClimbeyJumpEquipped());
    }
    
	public JumpTracker(Minecraft minecraft) {
		this.mc = minecraft;
	}
	public boolean isActive(EntityPlayerSP p){
		if(mc.vrSettings.seated)
			return false;
		if(!mc.vrSettings.vrFreeMove && !Minecraft.getMinecraft().vrSettings.simulateFalling)
			return false;
		if(!mc.vrSettings.realisticJumpEnabled)
			return false;
		if(p==null || p.isDead || !p.onGround)
			return false;
		if(p.isInWater() || p.isInLava())
			return false;
		if(p.isSneaking() || p.isRiding())
			return false;

		return true;
	}

	public boolean isjumping(){
		return c1Latched || c0Latched;
	}

	public void doProcess(EntityPlayerSP player){
		if(!isActive(player)) {
			c1Latched = false;
			c0Latched = false;
			return;
		}

		if(isClimbeyJumpEquipped()){

			IRoomscaleAdapter provider = mc.roomScale;

			boolean[] ok = new boolean[2];

			for(int c=0;c<2;c++){
				ok[c]=	mc.gameSettings.keyBindJump.isKeyDown();
			}

			boolean jump = false;
			if(!ok[0] && c0Latched){ //let go right
				mc.vrPlayer.triggerHapticPulse(0, 200);
				jump = true;
			}

			if(ok[0] && !c0Latched){ //grabbed right
				latchStart[0] = mc.roomScale.getControllerPos_Room(0).add(mc.roomScale.getControllerPos_Room(1)).scale(0.5);
				latchStartOrigin[0] = mc.roomScale.getRoomOriginPos_World();
				latchStartPlayer[0] = mc.player.getPositionVector();
				mc.vrPlayer.triggerHapticPulse(0, 1000);
			}

			if(!ok[1] && c1Latched){ //let go left
				mc.vrPlayer.triggerHapticPulse(1, 200);
				jump = true;
			}

			if(ok[1] && !c1Latched){ //grabbed left
				latchStart[1] = mc.roomScale.getControllerPos_Room(0).add(mc.roomScale.getControllerPos_Room(1)).scale(0.5);
				latchStartOrigin[1] = mc.roomScale.getRoomOriginPos_World();
				latchStartPlayer[1] = mc.player.getPositionVector();
				mc.vrPlayer.triggerHapticPulse(1, 1000);
			}

			c0Latched = ok[0];
			c1Latched = ok[1];

			int c =0;

			Vec3d now = mc.roomScale.getControllerPos_Room(0).add(mc.roomScale.getControllerPos_Room(1)).scale(0.5);

			Vec3d delta= now.subtract(latchStart[c]);

			Vec3d ro = latchStartOrigin[c].subtract(delta);
			Vec3d pl = latchStartPlayer[c].subtract(delta);

			if(!jump && isjumping()){ //bzzzzzz
				mc.vrPlayer.triggerHapticPulse(0, 200);
				mc.vrPlayer.triggerHapticPulse(1, 200);
			}

			if(jump){
				Vec3d m = (MCOpenVR.controllerHistory[0].netMovement(0.3).add(MCOpenVR.controllerHistory[1].netMovement(0.3))).scale(1.0f);
				if (player.isPotionActive(MobEffects.JUMP_BOOST))
					m=m.scale((player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1.5));
				if(delta.yCoord < 0 && m.yCoord < 0){

					player.motionX=-m.xCoord;
					player.motionY=-m.yCoord;
					player.motionZ=-m.zCoord;

					player.lastTickPosX = pl.xCoord;
					player.lastTickPosY = pl.yCoord;
					player.lastTickPosZ = pl.zCoord;			
					pl = pl.addVector(player.motionX, player.motionY, player.motionZ);					
					player.setPosition(pl.xCoord, pl.yCoord, pl.zCoord);
					mc.vrPlayer.snapRoomOriginToPlayerEntity(player, false, false, 0);
					
				} else {
					mc.vrPlayer.setRoomOrigin(latchStartOrigin[0].xCoord, latchStartOrigin[0].yCoord, latchStartOrigin[0].zCoord, false, false);
				}
			}else if(isjumping()){
				mc.vrPlayer.setRoomOrigin(ro.xCoord, ro.yCoord, ro.zCoord, false, false);
			}
		}else {
			if(MCOpenVR.hmdPivotHistory.netMovement(0.25).yCoord > 0.1 &&
					MCOpenVR.hmdPivotHistory.latest().yCoord-AutoCalibration.getPlayerHeight() > mc.vrSettings.jumpThreshold
					){
				player.jump();
			}			
		}



	}
}
