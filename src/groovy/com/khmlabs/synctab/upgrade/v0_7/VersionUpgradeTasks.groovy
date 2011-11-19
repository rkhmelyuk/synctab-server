package com.khmlabs.synctab.upgrade.v0_7

import com.khmlabs.synctab.upgrade.UpgradeTask
import com.khmlabs.synctab.upgrade.UpgradeTasks

/**
 * The repository of upgrade for version 0.7
 */
class VersionUpgradeTasks implements UpgradeTasks {

    List<UpgradeTask> getTasks() {
        [new AddDefaultUserTagsTask()]
    }

}
