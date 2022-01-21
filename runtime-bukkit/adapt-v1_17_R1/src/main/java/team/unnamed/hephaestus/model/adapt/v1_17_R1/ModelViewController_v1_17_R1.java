package team.unnamed.hephaestus.model.adapt.v1_17_R1;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Vector3f;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import team.unnamed.creative.base.Vector3Float;
import team.unnamed.hephaestus.Bone;
import team.unnamed.hephaestus.view.BukkitModelView;
import team.unnamed.hephaestus.view.ModelViewController;
import team.unnamed.hephaestus.util.Vectors;

import java.util.Collections;

public class ModelViewController_v1_17_R1
        implements ModelViewController {

    private void summonBone(
            double yawRadians,
            BukkitModelView view,
            Location location,
            Bone bone,
            Vector3Float offset
    ) {
        World world = location.getWorld();

        // location computing
        Vector3Float position = bone.offset().add(offset);
        Vector3Float relativePos = Vectors.rotateAroundY(
                position,
                yawRadians
        );

        // spawning the bone armorstand
        // noinspection ConstantConditions
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        EntityArmorStand entity = new EntityArmorStand(EntityTypes.c, worldServer);

        entity.setLocation(
                location.getX() + relativePos.x(),
                location.getY() + relativePos.y(),
                location.getZ() + relativePos.z(),
                location.getYaw(),
                location.getPitch()
        );

        entity.setSilent(true);
        entity.setNoGravity(true);
        entity.setSmall(true);
        entity.setInvisible(true);

        ItemStack item = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        // noinspection ConstantConditions
        meta.setColor(Color.WHITE);
        meta.setCustomModelData(bone.customModelData());
        item.setItemMeta(meta);

        net.minecraft.world.item.ItemStack nmsItem =
                CraftItemStack.asNMSCopy(item);

        entity.setSlot(EnumItemSlot.f, nmsItem, true);

        Packets.send(
                view.getViewers(),
                new PacketPlayOutSpawnEntityLiving(entity),
                new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true),
                new PacketPlayOutEntityEquipment(
                        entity.getId(),
                        Collections.singletonList(new Pair<>(
                                EnumItemSlot.f,
                                nmsItem
                        ))
                )
        );

        view.getEntities().put(bone.name(), entity);

        for (Bone component : bone.children()) {
            summonBone(
                    yawRadians,
                    view,
                    location,
                    component,
                    position
            );
        }
    }

    @Override
    public void show(BukkitModelView view) {
        Location location = view.getLocation();
        double yawRadians = Math.toRadians(location.getYaw());
        for (Bone bone : view.model().bones()) {
            summonBone(yawRadians, view, location, bone, Vector3Float.ZERO);
        }
    }

    private void teleportBonesRecursively(
            double yawRadians,
            BukkitModelView view,
            Location location,
            Bone bone,
            Vector3Float offset
    ) {

        // location computing
        Vector3Float position = bone.offset().add(offset);
        Vector3Float relativePos = Vectors.rotateAroundY(position, yawRadians);

        EntityArmorStand entity = (EntityArmorStand) view.getEntities().get(bone.name());
        entity.setLocation(
                location.getX() + relativePos.x(),
                location.getY() + relativePos.y(),
                location.getZ() + relativePos.z(),
                location.getYaw(),
                location.getPitch()
        );

        Packets.send(
                view.getViewers(),
                new PacketPlayOutEntityTeleport(entity)
        );

        for (Bone component : bone.children()) {
            teleportBonesRecursively(
                    yawRadians,
                    view,
                    location,
                    component,
                    position
            );
        }
    }

    @Override
    public void teleport(BukkitModelView view, Location location) {
        double yaw = Math.toRadians(location.getYaw());
        for (Bone bone : view.model().bones()) {
            teleportBonesRecursively(yaw, view, location, bone, Vector3Float.ZERO);
        }
    }

    private void hideBone(BukkitModelView view, Bone bone) {
        EntityArmorStand entity = (EntityArmorStand) view.getEntities().get(bone.name());
        Packets.send(view.getViewers(), new PacketPlayOutEntityDestroy(entity.getId()));
        for (Bone component : bone.children()) {
            hideBone(view, component);
        }
    }

    @Override
    public void hide(BukkitModelView view) {
        for (Bone bone : view.model().bones()) {
            hideBone(view, bone);
        }
    }

    private void colorizeBoneAndChildren(BukkitModelView view, Bone bone, Color color) {
        colorizeBone(view, bone.name(), color);
        for (Bone child : bone.children()) {
            colorizeBoneAndChildren(view, child, color);
        }
    }

    @Override
    public void colorizeBone(BukkitModelView view, String boneName, Color color) {
        EntityArmorStand entity = (EntityArmorStand) view.getEntities().get(boneName);

        net.minecraft.world.item.ItemStack nmsItem = entity.getEquipment(EnumItemSlot.f);

        ItemStack item = nmsItem == null ? new ItemStack(Material.LEATHER_HORSE_ARMOR) : CraftItemStack.asBukkitCopy(nmsItem);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        //noinspection ConstantConditions
        meta.setColor(color);
        item.setItemMeta(meta);

        nmsItem = CraftItemStack.asNMSCopy(item);

        entity.setSlot(EnumItemSlot.f, nmsItem, true);

        Packets.send(
                view.getViewers(),
                new PacketPlayOutEntityEquipment(
                        entity.getId(),
                        Collections.singletonList(new Pair<>(
                                EnumItemSlot.f,
                                nmsItem
                        ))
                )
        );
    }

    @Override
    public void colorize(BukkitModelView view, Color color) {
        for (Bone bone : view.model().bones()) {
            colorizeBoneAndChildren(view, bone, color);
        }
    }

    @Override
    public void teleportBone(BukkitModelView view, String boneName, Location location) {
        EntityArmorStand entity = (EntityArmorStand) view.getEntities().get(boneName);
        entity.setLocation(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
        Packets.send(view.getViewers(), new PacketPlayOutEntityTeleport(entity));
    }

    @Override
    public void updateBoneModelData(BukkitModelView view, Bone bone, int modelData) {

        EntityArmorStand entity = (EntityArmorStand) view.getEntities().get(bone.name());
        net.minecraft.world.item.ItemStack nmsItem
                = entity.getEquipment(EnumItemSlot.f);

        ItemStack item = nmsItem == null ? new ItemStack(Material.LEATHER_HORSE_ARMOR) : CraftItemStack.asBukkitCopy(nmsItem);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        // noinspection ConstantConditions
        meta.setCustomModelData(modelData);
        if (nmsItem == null) {
            meta.setColor(Color.WHITE);
        }
        item.setItemMeta(meta);

        nmsItem = CraftItemStack.asNMSCopy(item);
        entity.setSlot(EnumItemSlot.f, nmsItem, true);

        Packets.send(
                view.getViewers(),
                new PacketPlayOutEntityEquipment(
                        entity.getId(),
                        Collections.singletonList(new Pair<>(
                                EnumItemSlot.f,
                                nmsItem
                        ))
                )
        );
    }

    @Override
    public void setBonePose(BukkitModelView view, String boneName, Vector3Float angle) {
        EntityArmorStand entity = (EntityArmorStand) view.getEntities().get(boneName);
        DataWatcher watcher = new DataWatcher(null);
        watcher.register(
                new DataWatcherObject<>(16, DataWatcherRegistry.k),
                new Vector3f(angle.x(), angle.y(), angle.z())
        );
        Packets.send(view.getViewers(), new PacketPlayOutEntityMetadata(entity.getId(), watcher, true));
    }

    private void showBoneIndividually(
            BukkitModelView view,
            Bone bone,
            Player player
    ) {
        EntityArmorStand entity = (EntityArmorStand) view.getEntities().get(bone.name());

        Packets.send(
                player,
                new PacketPlayOutSpawnEntityLiving(entity),
                new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true),
                new PacketPlayOutEntityEquipment(
                        entity.getId(),
                        Collections.singletonList(new Pair<>(
                                EnumItemSlot.f,
                                entity.getEquipment(EnumItemSlot.f)
                        ))
                )
        );

        for (Bone child : bone.children()) {
            showBoneIndividually(view, child, player);
        }
    }

    private void hideBoneIndividually(
            BukkitModelView view,
            Bone bone,
            Player player
    ) {
        EntityArmorStand entity = (EntityArmorStand) view.getEntities().get(bone.name());
        Packets.send(player, new PacketPlayOutEntityDestroy(entity.getId()));

        for (Bone child : bone.children()) {
            hideBoneIndividually(view, child, player);
        }
    }

    @Override
    public void showIndividually(BukkitModelView view, Player player) {
        for (Bone bone : view.model().bones()) {
            showBoneIndividually(view, bone, player);
        }
    }

    @Override
    public void hideIndividually(BukkitModelView view, Player player) {
        for (Bone bone : view.model().bones()) {
            hideBoneIndividually(view, bone, player);
        }
    }
}