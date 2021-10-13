package com.spartdark.mydistributeddatabaseapplication.provider;
import com.spartdark.mydistributeddatabaseapplication.been.Contacter;

import com.spartdark.mydistributeddatabaseapplication.ResourceTable;
import ohos.agp.components.BaseItemProvider;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.Text;
import ohos.app.Context;

import java.util.List;

public class ContactProvider extends BaseItemProvider {
    private List<Contacter> contactArray;

    private Context context;

    private AdapterClickListener adapterClickListener;

    public ContactProvider(Context context, List contactArray) {
        this.context = context;
        this.contactArray = contactArray;
    }
    @Override
    public int getCount() {
        return contactArray == null ? 0 : contactArray.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < contactArray.size() && position >= 0) {
            return contactArray.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Component getComponent(int position, Component componentPara, ComponentContainer componentContainer) {
        ViewHolder viewHolder = null;
        Component component = componentPara;
        if (component == null) {
            component = LayoutScatter.getInstance(context).parse(ResourceTable.Layout_item_contact,
                    null, false);
            viewHolder = new ViewHolder();
            Component componentText = component.findComponentById(ResourceTable.Id_name);
            if (componentText instanceof Text) {
                viewHolder.name = (Text) componentText;
            }
            componentText = component.findComponentById(ResourceTable.Id_phone);
            if (componentText instanceof Text) {
                viewHolder.phone = (Text) componentText;
            }
            viewHolder.delete = (Button) component.findComponentById(ResourceTable.Id_delete);
            viewHolder.edit = (Button) component.findComponentById(ResourceTable.Id_edit);
            component.setTag(viewHolder);
        } else {
            if (component.getTag() instanceof ViewHolder) {
                viewHolder = (ViewHolder) component.getTag();
            }
        }
        if (viewHolder != null) {
            viewHolder.name.setText(contactArray.get(position).getName());
            viewHolder.phone.setText(contactArray.get(position).getPhone());
            viewHolder.edit.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component component) {
                    if (adapterClickListener != null) {
                        adapterClickListener.edit(position);
                    }
                }
            });
            viewHolder.delete.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component component) {
                    if (adapterClickListener != null) {
                        adapterClickListener.delete(position);
                    }
                }
            });
        }
        return component;
    }

    private static class ViewHolder {
        private Text name;
        private Text phone;
        private Button edit;
        private Button delete;
    }

    /**
     * Defines the callback event interface.
     */
    public interface AdapterClickListener {
        void edit(int position);

        void delete(int position);
    }

    public void setAdapterClickListener(AdapterClickListener adapterClickListener) {
        this.adapterClickListener = adapterClickListener;
    }


}
