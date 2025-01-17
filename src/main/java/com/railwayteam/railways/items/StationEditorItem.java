package com.railwayteam.railways.items;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.StationListContainer;
import com.railwayteam.railways.blocks.StationSensorRailTileEntity;
import com.railwayteam.railways.capabilities.CapabilitySetup;
import com.railwayteam.railways.capabilities.StationListCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.ArrayList;
import java.util.Iterator;

public class StationEditorItem extends Item implements INamedContainerProvider {
  public static final String NAME = "station_editor_tool";

  private static final StringTextComponent MSG_ADD_SUCCESS = new StringTextComponent("added station to list");
  private static final StringTextComponent MSG_ADD_EXISTS  = new StringTextComponent("station already in list");

  private ArrayList<StationLocation> stationList;

  public StationEditorItem (Properties props) {
    super (props);
    stationList = new ArrayList<StationLocation>();
  }

  @Override
  public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
    return false;
  }

  public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
    if (!world.isRemote) {
    //  player.sendMessage(new StringTextComponent("checking for cart"));
      RayTraceResult result = Minecraft.getInstance().objectMouseOver;
      if (result.getType().equals(RayTraceResult.Type.ENTITY) && ((EntityRayTraceResult)result).getEntity() instanceof AbstractMinecartEntity) {
      //  player.sendMessage(new StringTextComponent("found"));
        handleInteractionWithMinecart(world, player, (AbstractMinecartEntity)((EntityRayTraceResult)result).getEntity());
      }
      else {
        //  player.sendMessage(new StringTextComponent("opened menu from nothing"));
        NetworkHooks.openGui((ServerPlayerEntity) player, this, buf -> {
          buf.writeString("player");
          if (stationList.isEmpty()) {
            buf.writeInt(0);
          } else {
            buf.writeInt(stationList.size());
            for (StationLocation loc : stationList) buf.writeString(loc.name);
          }
        });
      }
      return ActionResult.success(player.getHeldItem(hand));
    }
    return super.onItemRightClick(world, player, hand);
  } // */

  // /*
	public void handleInteractionWithMinecart(World world, PlayerEntity player, AbstractMinecartEntity target) {
  //  player.sendMessage(new StringTextComponent("opened menu from minecart"));
    if (world.isRemote) return;
    NetworkHooks.openGui((ServerPlayerEntity)player, this, buf -> {
      target.getCapability(CapabilitySetup.CAPABILITY_STATION_LIST).ifPresent(capability -> {
      //  ((StationListCapability)capability).clear();
      //  for (StationLocation loc : stationList) {
      //    ((StationListCapability)capability).add(loc.name);
      //  }
        buf.writeString("minecart" + target.getEntityId());
      //  buf.writeInt( ((StationListCapability)capability).length() );
      //  Iterator<String> list = ((StationListCapability)capability).iterate();
      //  while (list.hasNext()) buf.writeString(list.next());
        buf.writeInt(stationList.size());
        for (StationLocation station : stationList) buf.writeString(station.printCoords());
      });
    //  target.getCapability(CapabilitySetup.CAPABILITY_STATION_LIST).ifPresent(capability -> {
    //    player.sendMessage(new StringTextComponent("wrote list of size " + ((StationListCapability) capability).length() + " to cart"));
    //  });
    });
  } // */

  @Override
  public ActionResultType onItemUse(ItemUseContext context) {
    if (context.getWorld().isRemote()) return ActionResultType.PASS;
    if (context.getWorld().getTileEntity(context.getPos()) instanceof StationSensorRailTileEntity) {
      boolean found = false;
      for (StationLocation loc : stationList) {
        if (loc.isAt(context.getPos())) {
          found = true;
          context.getPlayer().sendMessage(MSG_ADD_EXISTS);
          break;
        }
      }
      if (!found) {
        stationList.add(new StationLocation(context.getPos()));
        context.getPlayer().sendMessage(MSG_ADD_SUCCESS);
      }
      return ActionResultType.CONSUME;
    }
    else return super.onItemUse(context);
  } // */

  public void updateStationList (ArrayList<String> updatedList) {
    ArrayList<StationLocation> update = new ArrayList<StationLocation>();
    for (StationLocation loc : stationList) {
      if (updatedList.contains(loc.printCoords())) {
        update.add(loc);
      }
    }
    //list = update;
  }

  @Override
  public ITextComponent getDisplayName () {
    return new StringTextComponent(getTranslationKey());
  }

  @Override
  public Container createMenu (int id, PlayerInventory inv, PlayerEntity player) {
  //  player.sendMessage(new StringTextComponent("Trying to create menu"));
    return new StationListContainer(id, inv, stationList); //, inv);
  }

}
