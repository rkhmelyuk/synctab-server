package com.khmlabs.synctab.upgrade.v0_7

import com.khmlabs.synctab.upgrade.UpgradeTask
import com.khmlabs.synctab.upgrade.UpgradeTasks

/**
 * The repository of upgrade to version 0.7 tasks.
 */
class VersionUpgradeTasks implements UpgradeTasks {

    List<UpgradeTask> getTasks() {
        [
                new AddDefaultUserTagsTask(),
                new DeviceToTagTask()
        ]
    }

}
