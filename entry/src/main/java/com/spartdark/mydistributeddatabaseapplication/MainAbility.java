package com.spartdark.mydistributeddatabaseapplication;

import com.spartdark.mydistributeddatabaseapplication.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.bundle.IBundleManager;

public class MainAbility extends Ability {
    private static final String DISTRIBUTED_DATASYNC = "ohos.permission.DISTRIBUTED_DATASYNC";

    private static final int PERMISSION_CODE = 20201203;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
        requestPermission();
    }

    private void requestPermission() {
        if (verifySelfPermission(DISTRIBUTED_DATASYNC) != IBundleManager.PERMISSION_GRANTED) {
            if (canRequestPermission(DISTRIBUTED_DATASYNC)) {
                requestPermissionsFromUser(
                        new String[]{DISTRIBUTED_DATASYNC}, PERMISSION_CODE);
            }
        }
    }
}
