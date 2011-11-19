package com.khmlabs.synctab.upgrade

import com.khmlabs.synctab.SettingsService
import org.apache.log4j.Logger
import org.springframework.context.ApplicationContext

/**
 * This class used to upgrade to the specified version using specified logic.
 *
 * @author Ruslan Khmelyuk
 */
class VersionUpgrade {

    private static final Logger log = Logger.getLogger(VersionUpgrade)

    final ApplicationContext applicationContext

    VersionUpgrade(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * The map with version and appropriate tasks.
     * Using linked map, as order is important when upgrade through all versions (e.g. staging, development etc.)
     */
    protected Map<String, Class<? extends UpgradeTasks>> versionUpgradeTasks = [
            '0.7': v0_7.VersionUpgradeTasks,
    ] as LinkedHashMap

    /**
     * Upgrade application through all version.
     * It's needed for development and staging environments.
     */
    void upgrade() {
        try {
            def lastUpgradedVersion = settingsService.get('lastUpgradedVersion')
            if (!lastUpgradedVersion) {

                def lastVersion = null
                versionUpgradeTasks.each { String version, Class<? extends UpgradeTasks> tasks ->
                    upgradeWithTasks(tasks.newInstance())
                    lastVersion = version
                }

                lastUpgradedVersion = lastVersion
                settingsService.save('lastUpgradedVersion', lastUpgradedVersion)

                log.info("Successfully upgrade to version $lastVersion.")
            }
            else {
                log.info("Don't upgraded application, as already done.")
            }
        }
        catch (Exception e) {
            log.error("Failed to upgrade application", e)
        }
    }

    /**
     * Upgrade to specified version.
     * No check whether specified version if before or after current version.
     * We are assuming, that version upgrade is in sequence, one-by-one.
     *
     * @param version the version to upgrade to.
     */
    void upgradeToVersion(String version) {
        try {
            def lastUpgradedVersion = settingsService.get('lastUpgradedVersion')
            if (lastUpgradedVersion != version) {

                final Class<? extends UpgradeTasks> upgradeTasksClass = versionUpgradeTasks[version]
                if (upgradeTasksClass) {
                    final UpgradeTasks upgradeTasks = upgradeTasksClass.newInstance()
                    if (upgradeTasks) {
                        upgradeWithTasks(upgradeTasks)
                    }

                    lastUpgradedVersion = version
                    settingsService.save('lastUpgradedVersion', lastUpgradedVersion)

                    log.info("Successfully upgrade to version $version.")
                }
            }
            else {
                log.info("Don't upgraded to version $version, as already done.")
            }
        }
        catch (Exception e) {
            log.error("Failed to upgrade to version $version", e)
        }
    }

    protected void upgradeWithTasks(UpgradeTasks upgradeTasks) {
        def tasks = upgradeTasks.tasks
        for (UpgradeTask each: tasks) {
            try {
                log.info("Start upgrading with task ${each.class}...")
                if (each.upgrade(applicationContext)) {
                    log.info("Finished upgrading with task ${each.class}")
                }
                else {
                    log.info("Failed to upgrade with task ${each.class}")
                }
            }
            catch (Exception e) {
                log.error("Error to update by task ${each.class}, skipping to next task", e)
            }
        }
    }

    protected SettingsService getSettingsService() {
        (SettingsService) applicationContext.getBean('settingsService')
    }
}
