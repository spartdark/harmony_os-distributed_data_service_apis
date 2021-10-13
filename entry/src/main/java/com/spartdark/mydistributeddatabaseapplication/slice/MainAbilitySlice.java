package com.spartdark.mydistributeddatabaseapplication.slice;

import com.spartdark.mydistributeddatabaseapplication.ResourceTable;
import com.spartdark.mydistributeddatabaseapplication.been.Contacter;
import com.spartdark.mydistributeddatabaseapplication.component.ContactComponent;
import com.spartdark.mydistributeddatabaseapplication.provider.ContactProvider;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ListContainer;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.IDialog;
import ohos.agp.window.dialog.ToastDialog;
import ohos.data.distributed.common.*;
import ohos.data.distributed.device.DeviceFilterStrategy;
import ohos.data.distributed.device.DeviceInfo;
import ohos.data.distributed.user.SingleKvStore;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainAbilitySlice extends AbilitySlice implements ContactProvider.AdapterClickListener {

    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100, "HiContact");

    private static final String LOG_FORMAT = "%{public}s: %{public}s";

    private static final String TAG = "ContactSlice";

    private static final String STORE_ID = "contact_db1";

    private static final int DIALOG_SIZE_WIDTH = 800;

    private static final int DIALOG_SIZE_HEIGHT = 800;

    private static final int SHOW_TIME = 1500;

    private ContactProvider contactAdapter;

    private List<Contacter> contactArray;

    private KvManager kvManager;

    private SingleKvStore singleKvStore;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        initDbManager();
        initList();
        initEvent();
        queryContact();
    }

    /**
     * Initialize click event
     */
    private void initEvent() {
        findComponentById(ResourceTable.Id_addContact).setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                addContact();
            }
        });
        findComponentById(ResourceTable.Id_sync).setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                syncContact();
            }
        });
    }

    /**
     * Initialize ListContainer
     */
    private void initList() {
        Component component = findComponentById(ResourceTable.Id_listContainer);
        ListContainer listContainer;
        if (component instanceof ListContainer) {
            listContainer = (ListContainer) component;
            contactArray = new ArrayList<Contacter>();
            contactAdapter = new ContactProvider(this, contactArray);
            listContainer.setItemProvider(contactAdapter);
            contactAdapter.setAdapterClickListener(this);
        }
    }

    /**
     * Synchronizing contacts data
     */
    private void syncContact() {
        List<ohos.data.distributed.device.DeviceInfo> deviceInfoList = kvManager.getConnectedDevicesInfo(DeviceFilterStrategy.NO_FILTER);
        List deviceIdList = new ArrayList<>();
        for (ohos.data.distributed.device.DeviceInfo deviceInfo : deviceInfoList) {
            deviceIdList.add(deviceInfo.getId());
        }
        HiLog.info(LABEL_LOG, LOG_FORMAT,TAG, "device size= " + deviceIdList.size());
        if (deviceIdList.size() == 0) {
            showTip("Networking failed.");
            return;
        }
        singleKvStore.registerSyncCallback(new SyncCallback() {
            @Override
            public void syncCompleted(Map map) {
                getUITaskDispatcher().asyncDispatch(new Runnable() {
                    @Override
                    public void run() {
                        HiLog.info(LABEL_LOG, LOG_FORMAT,TAG, "sync success");
                        queryContact();
                        showTip("Synchronized successfully.");
                    }
                });
                singleKvStore.unRegisterSyncCallback();
            }
        });
        singleKvStore.sync(deviceIdList, SyncMode.PUSH_PULL);
    }

    /**
     * Initializing Database Management
     */
    private void initDbManager() {
        kvManager = createManager();
        singleKvStore = createDb(kvManager);
        subscribeDb(singleKvStore);
    }

    /**
     * Creates a distributed database manager.
     *
     * @return Returns the distributed database manager.
     */
    private KvManager createManager() {
        KvManager manager = null;
        try {
            KvManagerConfig config = new KvManagerConfig(this);
            manager = KvManagerFactory.getInstance().createKvManager(config);
        }
        catch (KvStoreException exception) {
            HiLog.info(LABEL_LOG, LOG_FORMAT,TAG, "some exception happen");
        }
        return manager;
    }

    /**
     * Creates a SingleKvStore object using a specified kvManager.
     *
     * @param kvManager Indicates the kvManager.
     * @return Returns the SingleKvStore object.
     */
    private SingleKvStore createDb(KvManager kvManager) {
        SingleKvStore kvStore = null;
        try {
            Options options = new Options();
            options.setCreateIfMissing(true).setEncrypt(false).setKvStoreType(KvStoreType.SINGLE_VERSION);
            kvStore = kvManager.getKvStore(options, STORE_ID);
        } catch (KvStoreException exception) {
            HiLog.info(LABEL_LOG, LOG_FORMAT,TAG, "some exception happen");
        }
        return kvStore;
    }


    /**
     * Subscribing to All (Currently, Remote) Data Change Notifications of a Single-Version Distributed Database
     *
     * @param singleKvStore Data operation
     */
    private void subscribeDb(SingleKvStore singleKvStore) {
        KvStoreObserver kvStoreObserverClient = new KvStoreObserverClient();
        singleKvStore.subscribe(SubscribeType.SUBSCRIBE_TYPE_REMOTE, kvStoreObserverClient);
    }

    /**
     * Receive database messages
     */
    private class KvStoreObserverClient implements KvStoreObserver {
        @Override
        public void onChange(ChangeNotification notification) {
            getUITaskDispatcher().asyncDispatch(new Runnable() {
                @Override
                public void run() {
                    HiLog.info(LABEL_LOG, LOG_FORMAT,TAG, "come to auto sync");
                    queryContact();
                    showTip("Synchronized successfully.");
                }
            });
        }
    }

    /**
     * Query Local Contacts
     */
    private void queryContact() {
        List<ohos.data.distributed.common.Entry> entryList = singleKvStore.getEntries("");
        HiLog.info(LABEL_LOG, LOG_FORMAT,TAG,"entryList size" + entryList.size());
        contactArray.clear();
        try {
            for (Entry entry : entryList) {
                contactArray.add(new Contacter(entry.getValue().getString(), entry.getKey()));
            }
        } catch (KvStoreException exception) {
            HiLog.info(LABEL_LOG, LOG_FORMAT,TAG,"the value must be String");
        }
        contactAdapter.notifyDataChanged();
    }

    /**
     * Write key-value data to the single-version distributed database.
     *
     * @param key Stored key
     * @param value Stored value
     */
    private void writeData(String key, String value) {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
            return;
        }
        singleKvStore.putString(key, value);
        HiLog.info(LABEL_LOG, LOG_FORMAT,TAG, "writeContact key= " + key + " writeContact value= " + value);
    }

    /**
     * Deleting Key Value Data from the Single-Version Distributed Database
     *
     * @param key Deleted Key
     */
    private void deleteData(String key) {
        if (key.isEmpty()) {
            return;
        }
        singleKvStore.delete(key);
        HiLog.info(LABEL_LOG, LOG_FORMAT,TAG, "deleteContact key= " + key);
    }

    /**
     * Add Contact
     */
    private void addContact() {
        showDialog(null, null, new ContactComponent.DialogCallBack() {
            @Override
            public void result(String name, String phone) {
                writeData(phone, name);
                contactArray.add(new Contacter(name, phone));
                contactAdapter.notifyDataSetItemInserted(contactAdapter.getCount());
                queryContact();
            }
        });
    }

    /**
     * Display dialog box
     *
     * @param name Contacts
     * @param phone phone
     * @param dialogCallBack callback result
     */
    private void showDialog(String name, String phone, ContactComponent.DialogCallBack dialogCallBack) {
        CommonDialog commonDialog = new CommonDialog(this);
        ContactComponent component = new ContactComponent(this);
        component.initData(name, phone);
        component.setDialogCallBack(new ContactComponent.DialogCallBack() {
            @Override
            public void result(String nameInput, String phoneInput) {
                if (nameInput.isEmpty() || phoneInput.isEmpty()) {
                    showTip("Name and phone number are mandatory.");
                    return;
                }

                if (phone == null && phoneIsExist(phoneInput)) {
                    showTip("Phone number already exists.");
                    return;
                }

                if (dialogCallBack != null) {
                    dialogCallBack.result(nameInput, phoneInput);
                }
                commonDialog.remove();
            }
        });

        commonDialog.setAutoClosable(true);
        commonDialog.setContentCustomComponent(component);
        commonDialog.show();
    }

    /**
     * Checks whether the mobile phone number exists.
     *
     * @param phone Indicates the mobile phone number to check.
     * @return Returns a boolean value indicating whether the mobile phone number exists.
     */
    private boolean phoneIsExist(String phone) {
        List<ohos.data.distributed.common.Entry> entryList = singleKvStore.getEntries("");
        for (Entry entry : entryList) {
            if (entry.getKey().equals(phone)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Event triggered by clicking each edit button
     *
     * @param position Position of the item number
     */
    @Override
    public void edit(int position) {
        Contacter contacter = contactArray.get(position);
        showDialog(contacter.getName(), contacter.getPhone(), new ContactComponent.DialogCallBack() {
            @Override
            public void result(String name, String phone) {
                writeData(phone, name);
                contactArray.set(position, new Contacter(name, phone));
                contactAdapter.notifyDataSetItemChanged(position);
                queryContact();
            }
        });
    }

    /**
     * Event triggered by clicking each delete button
     *
     * @param position Position of the item number
     */
    @Override
    public void delete(int position) {
        CommonDialog commonDialog = new CommonDialog(this);
        commonDialog.setSize(DIALOG_SIZE_WIDTH, DIALOG_SIZE_HEIGHT);
        commonDialog.setAutoClosable(true);
        commonDialog.setTitleText("    Warning")
                .setContentText("    Delete the information?")
                .setButton(0, "Cancel", new IDialog.ClickedListener() {
                    @Override
                    public void onClick(IDialog iDialog, int i) {
                        iDialog.destroy();
                    }
                })
                .setButton(1, "OK", new IDialog.ClickedListener() {
                    @Override
                    public void onClick(IDialog iDialog, int i) {
                        if (position > contactArray.size() - 1) {
                            showTip("The information to delete does not exist.");
                            return;
                        }
                        deleteData(contactArray.get(position).getPhone());
                        contactArray.remove(position);
                        contactAdapter.notifyDataChanged();
                        showTip("Deleted successfully.");
                        iDialog.destroy();
                    }
                }).show();
    }

    /**
     * tip message
     *
     * @param message message
     */
    private void showTip(String message) {
        new ToastDialog(this).setAlignment(LayoutAlignment.CENTER)
                .setText(message).setDuration(SHOW_TIME).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        kvManager.closeKvStore(singleKvStore);
        kvManager.deleteKvStore(STORE_ID);
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
