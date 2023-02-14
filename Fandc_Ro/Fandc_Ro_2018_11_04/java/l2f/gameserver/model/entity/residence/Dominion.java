package l2f.gameserver.model.entity.residence;

import l2f.commons.dao.JdbcEntityState;
import l2f.gameserver.dao.DominionDAO;
import l2f.gameserver.data.xml.holder.EventHolder;
import l2f.gameserver.data.xml.holder.ResidenceHolder;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Skill;
import l2f.gameserver.model.entity.events.EventType;
import l2f.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.model.pledge.Clan;
import l2f.gameserver.network.serverpackets.SystemMessage2;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.templates.StatsSet;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.TreeIntSet;

public class Dominion extends Residence
{
	private final IntSet _flags = new TreeIntSet();
	private Castle _castle;
	private int _lordObjectId;

	public Dominion(StatsSet set)
	{
		super(set);
	}

	@Override
	public void init()
	{
		initEvent();

		_castle = ResidenceHolder.getInstance().getResidence(Castle.class, getId() - 80);
		_castle.setDominion(this);

		loadData();

		_siegeDate.setTimeInMillis(0);
		if (getOwner() != null)
		{
			final DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
			runnerEvent.registerDominion(this);
		}
	}

	@Override
	public void rewardSkills()
	{
		final Clan owner = getOwner();
		if (owner != null)
		{
			if (!_flags.contains(getId()))
				return;

			for (int dominionId : _flags.toArray())
			{
				final Dominion dominion = ResidenceHolder.getInstance().getResidence(Dominion.class, dominionId);
				for (Skill skill : dominion.getSkills())
				{
					owner.addSkill(skill, false);
					owner.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill));
				}
			}
		}
	}

	@Override
	public void removeSkills()
	{
		final Clan owner = getOwner();
		if (owner != null)
		{
			for (int dominionId : _flags.toArray())
			{
				final Dominion dominion = ResidenceHolder.getInstance().getResidence(Dominion.class, dominionId);
				for (Skill skill : dominion.getSkills())
					owner.removeSkill(skill.getId());
			}
		}
	}

	public void addFlag(int dominionId)
	{
		_flags.add(dominionId);
	}

	public void removeFlag(int dominionId)
	{
		_flags.remove(dominionId);
	}

	public int[] getFlags()
	{
		return _flags.toArray();
	}

	public IntSet getFlagsList()
	{
		return _flags;
	}
	
	@Override
	public ResidenceType getType()
	{
		return ResidenceType.Dominion;
	}

	@Override
	protected void loadData()
	{
		DominionDAO.getInstance().select(this);
	}

	@Override
	public void changeOwner(Clan clan)
	{
		int newLordObjectId;
		if (clan == null)
		{
			if (_lordObjectId > 0)
				newLordObjectId = 0;
			else
				return;
		}
		else
		{
			newLordObjectId = clan.getLeaderId();

			SystemMessage2 message = new SystemMessage2(SystemMsg.CLAN_LORD_C2_WHO_LEADS_CLAN_S1_HAS_BEEN_DECLARED_THE_LORD_OF_THE_S3_TERRITORY).addName(clan.getLeader().getPlayer()).addString(clan.getName()).addResidenceName(getCastle());
			for (Player player : GameObjectsStorage.getAllPlayersForIterate())
				player.sendPacket(message);
		}

		_lordObjectId = newLordObjectId;

		setJdbcState(JdbcEntityState.UPDATED);
		update();

		for (NpcInstance npc : GameObjectsStorage.getAllNpcsForIterate())
			if (npc.getDominion() == this)
				npc.broadcastCharInfoImpl();
	}

	public int getLordObjectId()
	{
		return _lordObjectId;
	}

	@Override
	public Clan getOwner()
	{
		return _castle.getOwner();
	}

	@Override
	public int getOwnerId()
	{
		return _castle.getOwnerId();
	}

	public Castle getCastle()
	{
		return _castle;
	}

	@Override
	public void update()
	{
		DominionDAO.getInstance().update(this);
	}

	public void setLordObjectId(int lordObjectId)
	{
		_lordObjectId = lordObjectId;
	}
}
