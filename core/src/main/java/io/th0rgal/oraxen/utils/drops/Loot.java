package io.th0rgal.oraxen.utils.drops;

import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.compatibilities.provided.ecoitems.WrappedEcoItem;
import io.th0rgal.oraxen.compatibilities.provided.mythiccrucible.WrappedCrucibleItem;
import io.th0rgal.oraxen.items.ItemUpdater;
import io.th0rgal.oraxen.utils.ParseUtils;
import net.Indyuce.mmoitems.MMOItems;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Loot {

    private final String sourceID;
    private ItemStack itemStack;
    private final double probability;
    private final IntRange amount;
    private LinkedHashMap<String, Object> config;

    public Loot(LinkedHashMap<String, Object> config, String sourceID) {
        this.probability = ParseUtils.parseDouble(config.getOrDefault("probability", 1).toString(), 1);
        if (config.getOrDefault("amount", "") instanceof String amount && amount.contains("..")) {
            int minAmount = Integer.getInteger(StringUtils.substringBefore(amount, ".."), 1);
            int maxAmount = Math.max(Integer.getInteger(StringUtils.substringAfter(amount, ".."), 1), minAmount);
            this.amount = new IntRange(minAmount, maxAmount);
        } else this.amount = new IntRange(1,1);
        this.config = config;
        this.sourceID = sourceID;
    }

    public Loot(ItemStack itemStack, double probability) {
        this.itemStack = itemStack;
        this.probability = Math.min(1.0, probability);
        this.amount = new IntRange(1,1);
        this.sourceID = null;
    }

    public Loot(String sourceID, ItemStack itemStack, double probability, int minAmount, int maxAmount) {
        this.sourceID = sourceID;
        this.itemStack = itemStack;
        this.probability = Math.min(1.0, probability);
        this.amount = new IntRange(minAmount, maxAmount);
    }

    public ItemStack itemStack() {
        if (itemStack != null) return ItemUpdater.updateItem(itemStack);

        if (config.containsKey("oraxen_item")) {
            String itemId = config.get("oraxen_item").toString();
            itemStack = OraxenItems.getItemById(itemId).build();
        } else if (config.containsKey("crucible_item")) {
            itemStack = new WrappedCrucibleItem(config.get("crucible_item").toString()).build();
        } else if (config.containsKey("mmoitems_id") && config.containsKey("mmoitems_type")) {
            String type = config.get("mmoitems_type").toString();
            String id = config.get("mmoitems_id").toString();
            itemStack = MMOItems.plugin.getItem(type, id);
        } else if (config.containsKey("ecoitem")) {
            itemStack = new WrappedEcoItem(config.get("ecoitem").toString()).build();
        } else if (config.containsKey("minecraft_type")) {
            String itemType = config.get("minecraft_type").toString();
            Material material = Material.getMaterial(itemType);
            itemStack = material != null ? new ItemStack(material) : null;
        } else if (config.containsKey("minecraft_item")) {
            itemStack = (ItemStack) config.get("minecraft_item");
        }

        if (itemStack == null) itemStack = OraxenItems.getItemById(sourceID).build();

        return ItemUpdater.updateItem(itemStack);
    }

    public Loot itemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    public double probability() {
        return probability;
    }

    public IntRange amount() {
        return this.amount;
    }

    public void dropNaturally(Location location, int amountMultiplier) {
        if (Math.random() <= probability)
            dropItems(location, amountMultiplier);
    }

    public ItemStack getItem(int amountMultiplier) {
        ItemStack stack = itemStack().clone();
        int dropAmount = ThreadLocalRandom.current().nextInt(amount.getMinimumInteger(), amount.getMaximumInteger() + 1);
        stack.setAmount(stack.getAmount() * amountMultiplier * dropAmount);
        return ItemUpdater.updateItem(stack);
    }

    private void dropItems(Location location, int amountMultiplier) {
        if (location.getWorld() != null) location.getWorld().dropItemNaturally(location, getItem(amountMultiplier));
    }
}
