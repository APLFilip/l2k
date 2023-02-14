package zones;

import l2f.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Zone;
import l2f.gameserver.network.serverpackets.components.CustomMessage;
import l2f.gameserver.scripts.ScriptFile;
import l2f.gameserver.utils.ReflectionUtils;
import l2f.gameserver.utils.Location;

/**
 * Created by dkn on 18-Oct-16.
 */
public class BelethWater implements ScriptFile {

    private static ZoneListener _zoneListener;

    @Override
    public void onLoad() {
        _zoneListener = new ZoneListener();
        Zone zone = ReflectionUtils.getZone("[beleth_water]");
        zone.addListener(_zoneListener);
    }

    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {

    }

    public class ZoneListener implements OnZoneEnterLeaveListener {
        @Override
        public void onZoneEnter(Zone zone, Creature cha) {
            if (zone.getParams() == null || !cha.isPlayable() || cha.getPlayer().isGM())
                return;
            if (cha.getLevel() > zone.getParams().getInteger("levelLimit")) {
                if (cha.isPlayer())
                    cha.getPlayer().sendMessage(new CustomMessage("scripts.zones.epic.banishMsg", (Player) cha));
                cha.teleToLocation(Location.parseLoc(zone.getParams().getString("tele")));
            }
        }

        @Override
        public void onZoneLeave(Zone zone, Creature cha) {

        }

        @Override
        public void onEquipChanged(Zone zone, Creature actor)
        {
        }
    }
}
