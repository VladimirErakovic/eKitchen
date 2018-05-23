package mkitbs.ekitchen.app.entities;

import java.util.Date;

/**
 * Created by verakovic on 24.01.2017.
 */

public class Configuration {

    private int confId;
    private String serverIpAddress;
    private String appVersion;
    private Date installationTime;
    private int kitchenRoleId;
    private int hallRoleId;
    private int waiterRoleId;
    private int customerRoleId;

    public Configuration() {}

    public Configuration(int conf_id, String server_ip, String app_version, Date install_time, int kitchen_role,
                         int hall_role, int waiter_role, int customer_role) {
        this.confId = conf_id;
        this.serverIpAddress = server_ip;
        this.appVersion = app_version;
        this.installationTime = install_time;
        this.kitchenRoleId = kitchen_role;
        this.hallRoleId = hall_role;
        this.waiterRoleId = waiter_role;
        this.customerRoleId = customer_role;
    }

    public int getConfId() {
        return confId;
    }

    public void setConfId(int confId) {
        this.confId = confId;
    }

    public String getServerIpAddress() {
        return serverIpAddress;
    }

    public void setServerIpAddress(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Date getInstallationTime() {
        return installationTime;
    }

    public void setInstallationTime(Date installationTime) {
        this.installationTime = installationTime;
    }

    public int getKitchenRoleId() {
        return kitchenRoleId;
    }

    public void setKitchenRoleId(int kitchenRoleId) {
        this.kitchenRoleId = kitchenRoleId;
    }

    public int getHallRoleId() {
        return hallRoleId;
    }

    public void setHallRoleId(int hallRoleId) {
        this.hallRoleId = hallRoleId;
    }

    public int getWaiterRoleId() {
        return waiterRoleId;
    }

    public void setWaiterRoleId(int waiterRoleId) {
        this.waiterRoleId = waiterRoleId;
    }

    public int getCustomerRoleId() {
        return customerRoleId;
    }

    public void setCustomerRoleId(int customerRoleId) {
        this.customerRoleId = customerRoleId;
    }
}
