package com.khmlabs.synctab.upgrade

import org.springframework.context.ApplicationContext

/**
 * Single upgrade task
 * 
 * @author Ruslan Khmelyuk
 */
public interface UpgradeTask {

    boolean upgrade(ApplicationContext appContext)

}