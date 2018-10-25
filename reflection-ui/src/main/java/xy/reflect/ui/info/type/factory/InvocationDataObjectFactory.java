package xy.reflect.ui.info.type.factory;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ParameterAsFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class InvocationDataObjectFactory {

	protected ReflectionUI reflectionUI;
	protected IMethodInfo method;
	protected String contextId;

	public InvocationDataObjectFactory(ReflectionUI reflectionUI, IMethodInfo method, String contextId) {
		this.reflectionUI = reflectionUI;
		this.method = method;
		this.contextId = contextId;
	}

	public Instance getInstance(Object object, InvocationData invocationData) {
		Instance result = new InvocationDataObjectFactory.Instance(object, invocationData);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new TypeInfo());
		return result;
	}

	public Object unwrapInstanceObject(Object obj) {
		if (obj == null) {
			return null;
		}
		Instance instance = (Instance) obj;
		if (!instance.getOuterType().equals(this)) {
			throw new ReflectionUIError();
		}
		return instance.getObject();
	}

	public InvocationData unwrapInstanceInvocationData(Object obj) {
		if (obj == null) {
			return null;
		}
		Instance instance = (Instance) obj;
		if (!instance.getOuterType().equals(this)) {
			throw new ReflectionUIError();
		}
		return instance.getInvocationData();
	}

	public ITypeInfoSource getInstanceTypeInfoSource(SpecificitiesIdentifier specificitiesIdentifier) {
		return new PrecomputedTypeInfoSource(new TypeInfo(), specificitiesIdentifier);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		InvocationDataObjectFactory other = (InvocationDataObjectFactory) obj;
		if (contextId == null) {
			if (other.contextId != null)
				return false;
		} else if (!contextId.equals(other.contextId))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodSetupObjectFactory [method=" + method + ", contextId=" + contextId + "]";
	}

	public class Instance {
		protected Object object;
		protected InvocationData invocationData;

		public Instance(Object object, InvocationData invocationData) {
			this.object = object;
			this.invocationData = invocationData;
		}

		public Object getObject() {
			return object;
		}

		public InvocationData getInvocationData() {
			return invocationData;
		}

		private InvocationDataObjectFactory getOuterType() {
			return InvocationDataObjectFactory.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((invocationData == null) ? 0 : invocationData.hashCode());
			result = prime * result + ((object == null) ? 0 : object.hashCode());
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
			Instance other = (Instance) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (invocationData == null) {
				if (other.invocationData != null)
					return false;
			} else if (!invocationData.equals(other.invocationData))
				return false;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Instance [object=" + object + ", invocationData=" + invocationData + "]";
		}

	}

	public class TypeInfo extends AbstractInfo implements ITypeInfo {

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(this, null);
		}

		@Override
		public CategoriesStyle getCategoriesStyle() {
			return CategoriesStyle.getDefault();
		}

		@Override
		public ResourcePath getFormBackgroundImagePath() {
			return null;
		}

		@Override
		public ColorSpecification getFormBackgroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormForegroundColor() {
			return null;
		}

		@Override
		public Dimension getFormPreferredSize() {
			return null;
		}

		@Override
		public boolean onFormVisibilityChange(Object object, boolean visible) {
			return false;
		}

		@Override
		public boolean canPersist() {
			return false;
		}

		@Override
		public void save(Object object, OutputStream out) {
		}

		@Override
		public void load(Object object, InputStream in) {
		}

		@Override
		public ResourcePath getIconImagePath() {
			return null;
		}

		@Override
		public FieldsLayout getFieldsLayout() {
			return FieldsLayout.VERTICAL_FLOW;
		}

		@Override
		public MethodsLayout getMethodsLayout() {
			return MethodsLayout.HORIZONTAL_FLOW;
		}

		@Override
		public MenuModel getMenuModel() {
			return new MenuModel();
		}

		@Override
		public boolean isConcrete() {
			return true;
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isImmutable() {
			return false;
		}

		@Override
		public boolean isModificationStackAccessible() {
			return true;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections.<IMethodInfo>emptyList();
		}

		@Override
		public List<IFieldInfo> getFields() {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (IParameterInfo param : method.getParameters()) {
				result.add(new FieldInfo(reflectionUI, param, this));
			}
			return result;
		}

		@Override
		public String getName() {
			return "MethodSetupObject [context=" + contextId + "]";
		}

		@Override
		public String getCaption() {
			return ReflectionUIUtils.composeMessage(method.getCaption(), "Execution");
		}

		@Override
		public String getOnlineHelp() {
			return method.getOnlineHelp();
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public String toString(Object object) {
			Instance instance = (Instance) object;
			return instance.invocationData.toString();
		}

		@Override
		public boolean canCopy(Object object) {
			ReflectionUIUtils.checkInstance(this, object);
			return false;
		}

		@Override
		public Object copy(Object object) {
			throw new ReflectionUIError();
		}

		@Override
		public boolean supportsInstance(Object object) {
			return object instanceof Instance;
		}

		@Override
		public void validate(Object object) throws Exception {
			Instance instance = (Instance) object;
			method.validateParameters(instance.getObject(), instance.getInvocationData());
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			TypeInfo other = (TypeInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			return true;
		}

		private InvocationDataObjectFactory getOuterType() {
			return InvocationDataObjectFactory.this;
		}

		@Override
		public String toString() {
			return "TypeInfo [of=" + getOuterType() + "]";
		}

	}

	public class FieldInfo extends ParameterAsFieldInfo {

		public FieldInfo(ReflectionUI reflectionUI, IParameterInfo param, ITypeInfo containingType) {
			super(reflectionUI, InvocationDataObjectFactory.this.method, param, containingType);
		}

		@Override
		public void setValue(Object object, Object value) {
			Instance instance = (Instance) object;
			instance.invocationData.provideParameterValue(param.getPosition(), value);
		}

		@Override
		public Object getValue(Object object) {
			Instance instance = (Instance) object;
			return instance.invocationData.getParameterValue(param.getPosition());
		}

		@Override
		public Object[] getValueOptions(Object object) {
			Instance instance = (Instance) object;
			return param.getValueOptions(instance.object);
		}

		private InvocationDataObjectFactory getOuterType() {
			return InvocationDataObjectFactory.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((param == null) ? 0 : param.hashCode());
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
			FieldInfo other = (FieldInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (param == null) {
				if (other.param != null)
					return false;
			} else if (!param.equals(other.param))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Field [of=TypeInfo [of=" + getOuterType() + "]]";
		}
	}

}