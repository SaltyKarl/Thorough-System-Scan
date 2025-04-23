package data.intel;

import java.util.Map;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.TSSCan_SalvageableValue;

public class TSScan_SalvageReportIntel extends BaseIntelPlugin {

    public enum SalvageValue {
        NONE("None"),
        LOW("Low"),
        MEDIUM("Moderate"),
        HIGH("High"),
        EXTREME("EXTREME!!!");
        private final String valueString;

        SalvageValue(String value) {
            this.valueString = value;
        }
        public String getValueString() {
            return valueString;
        }
    }

    protected boolean interruptedScan;
    protected StarSystemAPI system;
    protected long removalCheckTimestamp = 0;
    protected float daysUntilRemoveCheck = 1f;

    public TSScan_SalvageReportIntel(StarSystemAPI system, boolean interruptedScan)
    {
        this.system=system;
        this.interruptedScan=interruptedScan;
    }

    @Override
    public boolean shouldRemoveIntel() {
        if (!system.isCurrentLocation()) {
            float daysSince = Global.getSector().getClock().getElapsedDaysSince(removalCheckTimestamp);
            if (daysSince > daysUntilRemoveCheck) {
                SalvageValue value = TSSCan_SalvageableValue.getSystemSalvageableValue();
                if (value == SalvageValue.NONE) {
                    return true;
                }
                removalCheckTimestamp = Global.getSector().getClock().getTimestamp();
                daysUntilRemoveCheck = 3f + (float) Math.random() * 3f;
            }
        }
        return super.shouldRemoveIntel();
    }


    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
        Color h = Misc.getHighlightColor();

        SalvageValue value = TSSCan_SalvageableValue.getSystemSalvageableValue();
        Map<String,Float> itemCount = TSSCan_SalvageableValue.getItemAmount();

        bullet(info);

        if (interruptedScan)
        {
            info.addPara("The scan was interrupted before it reached its peak... Nothing of value was gained.", initPad, tc);
            return;
        }

        if (mode != ListInfoMode.IN_DESC) {
            Color highlight = h;
            if (value == SalvageValue.NONE) highlight = tc;
            info.addPara("Projected value: %s", initPad, tc, highlight, value.getValueString());
            initPad = 0f;
        }
        if (value != SalvageValue.NONE)
        {
            info.addPara("Expect to receive the following number of rares (i.e. more or less):", initPad, tc);
            initPad = 0f;
        }

        if (itemCount.get(Commodities.ALPHA_CORE) > 0) {
            float alphaCoreAmount = itemCount.get(Commodities.ALPHA_CORE);
            String s = (alphaCoreAmount > 1) ? "s" : "";
            s = "s";
            info.addPara("%s %s core" + s, initPad, tc, h, String.format("%.1f",alphaCoreAmount), "Alpha");
            initPad = 0f;
        }
        if (itemCount.get(Commodities.BETA_CORE) > 0) {
            float betaCoreAmount = itemCount.get(Commodities.BETA_CORE);
            String s = (betaCoreAmount > 1) ? "s" : "";
            s = "s";
            info.addPara("%s %s core" + s, initPad, tc, h, String.format("%.1f",betaCoreAmount), "Beta");
            initPad = 0f;
        }
        if (itemCount.get(Commodities.GAMMA_CORE) > 0) {
            float gammaCoreAmount = itemCount.get(Commodities.GAMMA_CORE);
            String s = (gammaCoreAmount > 1) ? "s" : "";
            s = "s";
            info.addPara("%s %s core" + s, initPad, tc, h, String.format("%.1f",gammaCoreAmount), "Gamma");
            initPad = 0f;
        }
        if (itemCount.get(Commodities.BLUEPRINTS) > 0) {
            float blueprintAmount = itemCount.get(Commodities.BLUEPRINTS);
            String s = (blueprintAmount > 1) ? "s" : "";
            s = "s";
            info.addPara("%s Ship Weapon or LPC BP" + s, initPad, tc, h, String.format("%.1f",itemCount.get(Commodities.BLUEPRINTS)));
            initPad = 0f;
        }
        if (itemCount.get(Items.TAG_MODSPEC) > 0) {
            float modspecAmount = itemCount.get(Items.TAG_MODSPEC);
            String s = (modspecAmount > 1) ? "s" : "";
            s = "s";
            info.addPara("%s Modspec" + s, initPad, tc, h, String.format("%.1f",itemCount.get(Items.TAG_MODSPEC)));
            initPad = 0f;
        }
        if (itemCount.get(Items.TAG_COLONY_ITEM) > 0) {
            float colonyitemAmount = itemCount.get(Items.TAG_COLONY_ITEM);
            String s = (colonyitemAmount > 1) ? "s" : "";
            s = "s";
            info.addPara("%s Colony item" + s, initPad, tc, h, String.format("%.1f",itemCount.get(Items.TAG_COLONY_ITEM)));
            initPad = 0f;
        }
        if (itemCount.get("special_items") > 0) {
            float special_itemsAmount = itemCount.get("special_items");
            String s = (special_itemsAmount > 1) ? "s" : "";
            s = "s";
            info.addPara("%s Misc item" + s, initPad, tc, h, String.format("%.1f",itemCount.get("special_items")));
            initPad = 0f;
        }

        unindent(info);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color tc = Misc.getTextColor();
        float opad = 10f;
        float imageWidth = width/2f;

        SalvageValue value = TSSCan_SalvageableValue.getSystemSalvageableValue();

        if (interruptedScan)
            info.addImage(Global.getSettings().getSpriteName("intel", "TSSInterrupted"), imageWidth, opad);
        else
        {
            switch (value) {
                case NONE: {
                    info.addImage(Global.getSettings().getSpriteName("intel", "TSSNoneValue"), imageWidth, opad);
                    break;
                }
                case LOW: {
                    info.addImage(Global.getSettings().getSpriteName("intel", "TSSLowValue"), imageWidth, opad);
                    break;
                }
                case MEDIUM: {
                    info.addImage(Global.getSettings().getSpriteName("intel", "TSSMediumValue"), imageWidth, opad);
                    break;
                }
                case HIGH: {
                    info.addImage(Global.getSettings().getSpriteName("intel", "TSSHighValue"), imageWidth, opad);
                    break;
                }
                case EXTREME: {
                    info.addImage(Global.getSettings().getSpriteName("intel", "TSSHighValue"), imageWidth, opad);
                }
            }
            if (value == SalvageValue.NONE) {
                info.addPara(system.getNameWithLowercaseTypeShort() + "There is no rare salvage detected in the system.", opad);
            }
            else {
                info.addPara(
                system.getNameWithLowercaseTypeShort() + "Rare salvage have been detected in the system. Estimated total value of %s .",
                opad, h, value.getValueString());
            }
            switch (value){
                case NONE: {
                    info.addPara("It's a shame, better luck next time.", opad);
                    break;
                }
                case LOW: {
                    info.addPara("Not bad, hoping we get less storms in hyperspace", opad);
                    break;
                }   
                case MEDIUM: {
                    info.addPara("Not a bad haul, we didn't came to the wrong system.", opad);
                    break;
                }
                case HIGH: {
                    info.addPara("We've come to the right place! Yay! So many rare responses!", opad);
                    break;
                }
                case EXTREME: {
                    info.addPara("This is just... I can't believe it!", opad);
                }
            }
        }
        addBulletPoints(info, ListInfoMode.IN_DESC);


        addLogTimestamp(info, tc, opad);

        addDeleteButton(info, width);
    }

    @Override
    public String getIcon() {
        SalvageValue value = TSSCan_SalvageableValue.getSystemSalvageableValue();
        if (interruptedScan)return Global.getSettings().getSpriteName("intel", "TSSInterrupted");
        if (value == SalvageValue.NONE)return Global.getSettings().getSpriteName("intel", "TSSNoneValue");
        if (value == SalvageValue.LOW)return Global.getSettings().getSpriteName("intel", "TSSLowValue");
        if (value == SalvageValue.MEDIUM)return Global.getSettings().getSpriteName("intel", "TSSMediumValue");
        return Global.getSettings().getSpriteName("intel", "TSSHighValue");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        //tags.add(Tags.INTEL_FLEET_LOG);
        SalvageValue value = TSSCan_SalvageableValue.getSystemSalvageableValue();
        if (value != SalvageValue.NONE) {
            tags.add(Tags.INTEL_EXPLORATION);
        }

        tags.add(Tags.INTEL_SALVAGE);
        return tags;
    }


    public String getName() {
        return "System-Scale Sensor Burst Results - " + system.getBaseName();
    }


    @Override
    public String getCommMessageSound() {
        return super.getCommMessageSound();
        //return "ui_discovered_entity";
    }

    public String getSortString() {
        return getSortStringNewestFirst();
    }
}