package xy.reflect.ui.control;

public class DefaultContext implements IContext {

	protected String identifier;

	public DefaultContext(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
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
		DefaultContext other = (DefaultContext) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultContext [identifier=" + identifier + "]";
	}

}
