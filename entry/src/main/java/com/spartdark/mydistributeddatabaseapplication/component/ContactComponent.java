package com.spartdark.mydistributeddatabaseapplication.component;

import com.spartdark.mydistributeddatabaseapplication.ResourceTable;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.app.Context;

public class ContactComponent extends DirectionalLayout {
    private Component component;

    private TextField nameTextField;

    private TextField phoneTextField;

    private Text title;

    private DialogCallBack dialogCallBack;

    public ContactComponent(Context context) {
        super(context);
        addComponent(context);
        initView();
        initEvent();
    }

    private void initEvent() {
        component.findComponentById(ResourceTable.Id_confirm).setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                confirmContact();
            }
        });
    }

    private void confirmContact() {
        String nameInput = nameTextField.getText();
        String phoneInput = phoneTextField.getText();
        if (dialogCallBack != null) {
            dialogCallBack.result(nameInput, phoneInput);
        }
    }

    private void initView() {
        Component componentText = component.findComponentById(ResourceTable.Id_name);
        if (componentText instanceof TextField) {
            nameTextField = (TextField) componentText;
        }
        componentText = component.findComponentById(ResourceTable.Id_phone);
        if (componentText instanceof TextField) {
            phoneTextField = (TextField) componentText;
        }
        componentText = component.findComponentById(ResourceTable.Id_title);
        if (componentText instanceof Text) {
            title = (Text) componentText;
        }
    }

    private void addComponent(Context context) {
        component = LayoutScatter.getInstance(context).parse(ResourceTable.Layout_item_dialog,
                null, false);
        addComponent(component);
        LayoutConfig layoutConfig = new LayoutConfig(LayoutConfig.MATCH_PARENT, LayoutConfig.MATCH_CONTENT);
        setLayoutConfig(layoutConfig);
    }

    public void initData(String name, String phone) {
        nameTextField.setBubbleSize(0,0);
        phoneTextField.setBubbleSize(0,0);
        if (name != null) {
            nameTextField.setText(name);
        }
        if (phone != null) {
            phoneTextField.setText(phone);
            phoneTextField.setEnabled(false);
            title.setText("Modify information");
        }
    }

    public void setDialogCallBack(DialogCallBack dialogCallBack) {
        this.dialogCallBack = dialogCallBack;
    }

    public interface DialogCallBack {
        void result(String name, String phone);
    }
}
