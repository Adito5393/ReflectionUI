package xy.reflect.ui.info.menu;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import xy.reflect.ui.info.menu.builtin.ExitMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.HelpMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.OpenMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.RedoMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.ResetMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.SaveAsMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.SaveMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.UndoMenuItem;

public class Menu extends AbstractMenuItem implements IMenuItemContainer {

	public Menu(String name) {
		super(name);
	}

	public Menu() {
	}

	private static final long serialVersionUID = 1L;

	protected List<AbstractMenuItem> items = new ArrayList<AbstractMenuItem>();
	protected List<MenuItemCategory> itemCategories = new ArrayList<MenuItemCategory>();

	@Override
	@XmlElements({ @XmlElement(name = "menu", type = Menu.class),
			@XmlElement(name = "exitMenuItem", type = ExitMenuItem.class),
			@XmlElement(name = "helpMenuItem", type = HelpMenuItem.class),
			@XmlElement(name = "undoMenuItem", type = UndoMenuItem.class),
			@XmlElement(name = "redoMenuItem", type = RedoMenuItem.class),
			@XmlElement(name = "resetMenuItem", type = ResetMenuItem.class),
			@XmlElement(name = "openMenuItem", type = OpenMenuItem.class),
			@XmlElement(name = "saveMenuItem", type = SaveMenuItem.class),
			@XmlElement(name = "saveAsMenuItem", type = SaveAsMenuItem.class) })
	public List<AbstractMenuItem> getItems() {
		return items;
	}

	public void setItems(List<AbstractMenuItem> items) {
		this.items = items;
	}

	public void addItem(AbstractMenuItem item) {
		this.items.add(item);
	}

	public List<MenuItemCategory> getItemCategories() {
		return itemCategories;
	}

	public void setItemCategories(List<MenuItemCategory> itemCategories) {
		this.itemCategories = itemCategories;
	}

	public void addItemCategory(MenuItemCategory itemCategory) {
		this.itemCategories.add(itemCategory);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((itemCategories == null) ? 0 : itemCategories.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Menu other = (Menu) obj;
		if (itemCategories == null) {
			if (other.itemCategories != null)
				return false;
		} else if (!itemCategories.equals(other.itemCategories))
			return false;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Menu [name=" + name + "]";
	}

}