package dev.anarchy.fspatches.patches.utils;

import fr.paladium.palajobs.api.type.JobType;
import fr.paladium.palajobs.core.jobs.AbstractJob;
import fr.paladium.palajobs.core.jobs.BlackListedItem;
import fr.paladium.palajobs.core.registry.JobRegistry;
import fr.paladium.palajobs.server.managers.JobsManager;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UtilityClass
public class JobCustomizer {

    @SneakyThrows
    public static void clearAllBlacklists() {
        JobsManager manager = JobsManager.getInstance();

        if (manager == null) {
            System.out.println("[Fspatches] Le JobsManager n'est pas chargé !");
            return;
        }

        Field craftsField = JobsManager.class.getDeclaredField("blackListedCrafts");
        craftsField.setAccessible(true);
        List<?> craftsList = (List<?>) craftsField.get(manager);
        if (craftsList != null) {
            craftsList.clear();
            System.out.println("[Fspatches] Blacklist des crafts vidée !");
        }

        Field usagesField = JobsManager.class.getDeclaredField("blackListedUsages");
        usagesField.setAccessible(true);
        List<?> usagesList = (List<?>) usagesField.get(manager);
        if (usagesList != null) {
            usagesList.clear();
            System.out.println("[Fspatche] Blacklist des usages vidée !");
        }

        for (Object obj : JobRegistry.getInstance().getJobs()) {
            if (obj instanceof AbstractJob) {
                AbstractJob job = (AbstractJob) obj;

                Map<?, ?> rewardsMap = job.getRewards();
                if (rewardsMap != null) {
                    rewardsMap.clear();
                }

                System.out.println("[Fspatches] Rewards vidés pour le métier : " + job.getType().name());
            }
        }
    }

    private static void addBlacklistedCraft(JobType type, int level, ItemStack item) {
        BlackListedItem bItem = new BlackListedItem(level, type, item);
        JobsManager.getInstance().registerBlackListedCraft(bItem);
    }

    private static void addBlacklistedUsage(JobType type, int level, ItemStack item) {
        BlackListedItem bItem = new BlackListedItem(level, type, item);
        JobsManager.getInstance().registerBlackListedUsage(bItem);
    }

    @SneakyThrows
    public static void customBlacklist() {
        Field field = JobsManager.class.getDeclaredField("blackListedCrafts");
        field.setAccessible(true);
        field.get(JobsManager.getInstance()).getClass().getMethod("clear").invoke(null);
        field.set(JobsManager.getInstance(), new ArrayList<BlackListedItem>());
        // crafts
    }

    @SneakyThrows
    public static void customUsageBlacklist() {
        Field field = JobsManager.class.getDeclaredField("blackListedUsages");
        field.setAccessible(true);
        field.get(JobsManager.getInstance()).getClass().getMethod("clear").invoke(null);
        field.set(JobsManager.getInstance(), new ArrayList<BlackListedItem>());

        // usages

    }
}
