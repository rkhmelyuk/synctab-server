package com.khmlabs.synctab

class SettingsService {

    static transactional = false

    /**
     * Gets setting value by name.
     *
     * @param name the setting name
     * @return the value.
     */
    String get(String name) {
        Setting.findByName(name)?.value
    }

    /**
     * Saves the setting new value.
     *
     * @param name the setting name.
     * @param value the new setting value.
     */
    void save(String name, String value) {
        def setting = Setting.findByName(name)
        if (!setting) {
            setting = new Setting()
            setting.name = name
        }

        setting.value = value
        setting.save(flush: true)
    }
}
