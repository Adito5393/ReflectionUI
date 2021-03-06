/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.info.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a menu.
 * 
 * @author olitank
 *
 */
public class Menu extends AbstractMenuItem implements IMenuItemContainer {

	public Menu(String name) {
		super(name);
	}

	public Menu() {
	}

	protected List<AbstractMenuItem> items = new ArrayList<AbstractMenuItem>();
	protected List<MenuItemCategory> itemCategories = new ArrayList<MenuItemCategory>();

	@Override
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
