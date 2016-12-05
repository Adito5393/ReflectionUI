package xy.reflect.ui.info.type.enumeration;

import java.util.Map;

public class EnumerationItemInfoProxy implements IEnumerationItemInfo {

	protected IEnumerationItemInfo base;

	public EnumerationItemInfoProxy(IEnumerationItemInfo base) {
		this.base = base;
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnumerationItemInfoProxy other = (EnumerationItemInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return base.toString();
	}

	
}