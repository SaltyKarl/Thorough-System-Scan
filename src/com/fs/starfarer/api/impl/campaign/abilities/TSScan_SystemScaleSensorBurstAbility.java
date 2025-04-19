package com.fs.starfarer.api.impl.campaign.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.CampaignTerrain;
import data.TSScan_Constants;
import data.intel.TSScan_SalvageReportIntel;
import data.scripts.TSScan_CRLoss;
import data.scripts.TSScan_EntityDiscover;
import data.scripts.TSScan_SystemScanPointsManager;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TSScan_SystemScaleSensorBurstAbility extends BaseDurationAbility {

	private static TSScan_EntityDiscover nowDiscovery=null;

	private static LocationAPI initialLocation;

	@Override
	protected void activateImpl() {
		if (entity.isInCurrentLocation()) {
            initialLocation=getFleet().getContainingLocation();
			if (!Global.getSettings().isDevMode())
			{
				TSScan_CRLoss.CRLoss(false,null);
				getFleet().getCargo().removeCommodity(Commodities.VOLATILES,(int)computeVolatileCost());
			}
			Global.getSector().addPing(entity, TSScan_Constants.SYSTEM_SCALE_SENSOR_BURST);
		}
	}

	@Override
	protected void applyEffect(float amount, float level) {

		if (getFleet().getContainingLocation()!=initialLocation)
		{
			deactivate();
			return;
		}
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;

		fleet.getStats().getSensorRangeMod().modifyFlat(getModId(), -MathUtils.getRandomNumberInRange(0,(int)fleet.getSensorStrength()), "System Wide Sensor Scan");
		fleet.getStats().getSensorProfileMod().modifyFlat(getModId(), 30000f, "System Wide Sensor Scan");
		if (level>=.8f)
		{
			if (nowDiscovery == null)
			{
				nowDiscovery = new TSScan_EntityDiscover(fleet.getStarSystem());
				Global.getSector().addPing(entity, TSScan_Constants.SYSTEM_SCALE_SENSOR_BURST_PING_FINISH);
			}
			fleet.getStats().getSensorRangeMod().modifyFlat(getModId(), 30000f, "System Wide Sensor Scan");
		}
		fleet.goSlowOneFrame();
	}

	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}

	@Override
	protected void cleanupImpl() {
		Global.getSector().addPing(entity, TSScan_Constants.SYSTEM_SCALE_SENSOR_BURST_PING_END);
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		if (nowDiscovery!=null)nowDiscovery.recoverEntities();

		if (TSScan_Constants.REPORT_SHOULD_DISPLAY)
			Global.getSector().getIntelManager().addIntel(new TSScan_SalvageReportIntel(fleet.getStarSystem()));

		nowDiscovery=null;
		fleet.getStats().getSensorRangeMod().unmodify(getModId());
		fleet.getStats().getSensorProfileMod().unmodify(getModId());
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color alarm = Color.red;

		tooltip.addTitle(spec.getName());

		float pad = 10f;
		tooltip.addPara("Overloading fleet sensor arrays to obtain virtually all information system-wide", pad);
		tooltip.addPara("Temporarily extends sensor range to system wide and dramatically increases the detection range of your fleet.When this ability is active the fleet can only %s",
				pad, highlight,"Move slowly");
		tooltip.addPara("Due to the gravitational interference, System Wide Sensor Scan can only be performed in the vicinity of the L4 or L5 point of the highest-orbiting object if the system contains objects other than the central object.",
				pad);
		if (getFleet().getSensorRangeMod().computeEffective(getFleet().getSensorStrength())< TSScan_Constants.SENSOR_STRENGTH_NEEDED)
		{
			tooltip.addPara("WARNING: Your fleet's sensor strength must be at %s in order to perform System Wide Sensor Scan!", pad, alarm, highlight, ""+TSScan_Constants.SENSOR_STRENGTH_NEEDED);
			return;
		}
		if (getFleet().isInHyperspace())
			tooltip.addPara("WARNING: System Wide Sensor Scan can not be used in hyperspace!",alarm,pad);
		else {
			tooltip.addPara("Scanning the current system requires %s units of volatiles.",pad,highlight,""+(int)computeVolatileCost());
			if ((int) computeVolatileCost() > getFleet().getCargo().getCommodityQuantity(Commodities.VOLATILES))
				tooltip.addPara("WARNING: Your have insufficient reserves of volatiles!", alarm, pad);
			else if (!isInScanLocation())
				tooltip.addPara("Go to the scan position to start a System Wide Sensor Scan", Color.orange, pad);
			else if (!isUsable())
				tooltip.addPara("Current location allows System Wide Sensor Scan", Color.yellow, pad);
			else  tooltip.addPara("Fleet can perform System Wide Sensor Scan", Color.green, pad);
		}
		TSScan_CRLoss.CRLoss(true,tooltip);
		if (!getNonReadyShips().isEmpty()&&!Global.getSettings().isDevMode())
		{
			tooltip.addPara("SERIOUS WARNING: The following ships are at low combat readiness and overloaded sensors may cause loss of crew and ship.",alarm,pad);
			List<FleetMemberAPI> members=getNonReadyShips();
			for (FleetMemberAPI member:members)
			{
				tooltip.addPara("   %s %s  "+member.getShipName()+", "+member.getHullSpec().getHullNameWithDashClass(),pad,highlight, String.format("%d%%", (int) (member.getRepairTracker().getCR() * 100)), String.format("%d%%", (int) (TSScan_CRLoss.calculateCRLoss(member))));
			}
		}
		tooltip.addPara("*2000 units = 1 map grid cell", gray, pad);
		tooltip.addPara("**A fleet is considered slow-moving at a burn level of half that of its slowest ship.", gray, 0f);
		tooltip.addPara("***frigate CR cost %s,destroyer CR cost %s,cruiser CR cost %s,Capital ship CR cost %s", 0f, gray,gray, String.format("%d%%", (int) (TSScan_CRLoss.baseCRDamage * TSScan_CRLoss.lossMultOfSize(ShipAPI.HullSize.FRIGATE))), String.format("%d%%", (int) (TSScan_CRLoss.baseCRDamage * TSScan_CRLoss.lossMultOfSize(ShipAPI.HullSize.DESTROYER))), String.format("%d%%", (int) (TSScan_CRLoss.baseCRDamage * TSScan_CRLoss.lossMultOfSize(ShipAPI.HullSize.CRUISER))), String.format("%d%%", (int) (TSScan_CRLoss.baseCRDamage * TSScan_CRLoss.lossMultOfSize(ShipAPI.HullSize.CAPITAL_SHIP))));

		addIncompatibleToTooltip(tooltip, expanded);

	}


	@Override
	public Color getCooldownColor() {
		if (showAlarm()) {
			Color color = Misc.getNegativeHighlightColor();
			return Misc.scaleAlpha(color, Global.getSector().getCampaignUI().getSharedFader().getBrightness() * 0.5f);
		}
		return super.getCooldownColor();
	}

	@Override
	public boolean isCooldownRenderingAdditive() {
		return showAlarm();
	}
	@Override
	public boolean isUsable() {
		return super.isUsable() &&
		(
			getFleet() != null &&
			!getFleet().isInHyperspace()&&
			(
				isInScanLocation()&&
				getFleet().getSensorRangeMod().computeEffective(getFleet().getSensorStrength())>= TSScan_Constants.SENSOR_STRENGTH_NEEDED&&
				computeVolatileCost() <= getFleet().getCargo().getCommodityQuantity(Commodities.VOLATILES)
			)||
			Global.getSettings().isDevMode()
		);
	}
	protected boolean showAlarm() {
		return !getNonReadyShips().isEmpty() && !isOnCooldown() && !isActiveOrInProgress() && isUsable();
	}
	protected List<FleetMemberAPI> getNonReadyShips() {
		List<FleetMemberAPI> result = new ArrayList<>();
		if (Global.getSettings().isDevMode())return result;
		CampaignFleetAPI fleet=getFleet();
		if (fleet == null) return result;
		List<FleetMemberAPI> members=TSScan_CRLoss.getSensorMembers();
		for (FleetMemberAPI member : members)
			if (member.getRepairTracker().getCR()*100f < -TSScan_CRLoss.calculateCRLoss(member))
				result.add(member);
		return result;
	}
	protected float computeVolatileCost()
	{
		if (Global.getSettings().isDevMode()||getFleet().isInHyperspace())return 0;
		double cost=0;
		List<SectorEntityToken> entities=getFleet().getStarSystem().getAllEntities();
		for (SectorEntityToken entity:entities)
		{
			if (entity instanceof AsteroidBeltTerrainPlugin)cost+=1;
			else if (entity instanceof PlanetAPI)
			{
				if (entity.hasTag(Tags.STAR)||entity.hasTag(Tags.GAS_GIANT))
				{
                    switch (((PlanetAPI) entity).getSpec().getPlanetType()) {
						case StarTypes.BLACK_HOLE:cost += TSScan_Constants.VOLATILE_MULT_BLACK_HOLE;
                        case StarTypes.NEUTRON_STAR:cost += TSScan_Constants.VOLATILE_MULT_NEUTRON_STAR;
                        case StarTypes.BLUE_SUPERGIANT:cost += TSScan_Constants.VOLATILE_MULT_BLUE_SUPERGIANT;
                        case StarTypes.RED_SUPERGIANT:cost += TSScan_Constants.VOLATILE_MULT_RED_SUPERGIANT;
                        case StarTypes.ORANGE_GIANT:cost += TSScan_Constants.VOLATILE_MULT_ORANGE_GIANT;
                        case StarTypes.RED_GIANT:cost += TSScan_Constants.VOLATILE_MULT_RED_GIANT;
                        case StarTypes.BLUE_GIANT:cost += TSScan_Constants.VOLATILE_MULT_BLUE_GIANT;
                        case StarTypes.YELLOW:cost += TSScan_Constants.VOLATILE_MULT_YELLOW;
                        case StarTypes.ORANGE:cost += TSScan_Constants.VOLATILE_MULT_ORANGE;
                        case StarTypes.WHITE_DWARF:cost += TSScan_Constants.VOLATILE_MULT_WHITE_DWARF;
                        case StarTypes.RED_DWARF:cost += TSScan_Constants.VOLATILE_MULT_RED_DWARF;
                        case StarTypes.BROWN_DWARF:cost += TSScan_Constants.VOLATILE_MULT_BROWN_DWARF;
                        case StarTypes.GAS_GIANT:cost += TSScan_Constants.VOLATILE_MULT_GAS_GIANT;
                        case StarTypes.ICE_GIANT:cost += TSScan_Constants.VOLATILE_MULT_ICE_GIANT;
                        default:cost += 0;
                    }
				}
				else if (entity.hasTag(Tags.PLANET))cost+= TSScan_Constants.VOLATILE_MULT_PLANET;
			}
			else if (entity.hasTag(Tags.ACCRETION_DISK))cost+= TSScan_Constants.VOLATILE_MULT_ACCRETION_DISK;
			else if (entity.hasTag(Tags.STABLE_LOCATION))cost+= TSScan_Constants.VOLATILE_MULT_STABLE_LOCATION;
			else if (entity.hasTag(Tags.GATE))cost+= TSScan_Constants.VOLATILE_MULT_GATE;
			else if (entity.hasTag(Tags.JUMP_POINT))cost+= TSScan_Constants.VOLATILE_MULT_JUMP_POINT;
			else if (entity.hasTag(Tags.SALVAGEABLE))cost+= TSScan_Constants.VOLATILE_MULT_SALVAGEABLE;
		}
		return (float)(cost* TSScan_Constants.VOLATILE_MULT);
	}

	public boolean isInScanLocation()
	{
		if (getFleet().isInHyperspace())return false;
		if (TSScan_SystemScanPointsManager.IgnoredSystems.contains(getFleet().getContainingLocation().getId()))return true;

		List<SectorEntityToken> ScanPoints=TSScan_SystemScanPointsManager.ScanPointsOfSystems.get(getFleet().getContainingLocation().getId());

		if (ScanPoints==null||ScanPoints.isEmpty())
		{
			TSScan_SystemScanPointsManager.reload(getFleet().getStarSystem());
			return isInScanLocation();
		}

		for (SectorEntityToken ScanPoint:ScanPoints)
			if (((CampaignTerrain)ScanPoint).getPlugin().containsEntity(getFleet()))
				return true;
		return false;
	}
}





