package xy.reflect.ui.info.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultiSubListField;
import xy.reflect.ui.info.field.MultiSubListField.VirtualItem;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.IListTypeInfo.IItemPosition;
import xy.reflect.ui.info.type.IListTypeInfo.IListStructuralInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public abstract  class AbstractTreeDetectionListStructuralInfo implements IListStructuralInfo {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo rootItemType;
	protected abstract  boolean isTabular() ;

	public AbstractTreeDetectionListStructuralInfo(ReflectionUI reflectionUI,
			ITypeInfo rootItemType) {
		this.reflectionUI = reflectionUI;
		this.rootItemType = rootItemType;
	}

	@Override
	public IFieldInfo getItemSubListField(IItemPosition itemPosition) {
		List<IFieldInfo> candidateFields = getItemSubListCandidateFields(itemPosition);
		for (int i = 0; i < candidateFields.size(); i++) {
			candidateFields.set(i, new HiddenNullableFacetFieldInfoProxy(
					reflectionUI, candidateFields.get(i)));
		}
		if (candidateFields.size() == 0) {
			return null;
		} else if (candidateFields.size() == 1) {
			return candidateFields.get(0);
		} else {
			return new MultiSubListField(reflectionUI, candidateFields);
		}
	}

	protected List<IFieldInfo> getItemSubListCandidateFields(
			IItemPosition itemPosition) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		ITypeInfo itemType = itemPosition.getContainingListType().getItemType();
		Object item = itemPosition.getItem();
		ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(item));
		if ((actualItemType instanceof IMapEntryTypeInfo)
				&& (itemPosition.getParentItemPosition() != null)) {
			IMapEntryTypeInfo entryType = (IMapEntryTypeInfo) actualItemType;
			IFieldInfo entryValueField = entryType.getValueField();
			ITypeInfo entryValueType = entryValueField.getType();
			if (entryValueType instanceof IListTypeInfo) {
				if (!isTabular()) {
					result.add(entryValueField);
				} else {
					ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType)
							.getItemType();
					ITypeInfo parentListItemType = itemPosition
							.getParentItemPosition().getContainingListType()
							.getItemType();
					if (ReflectionUIUtils.equalsOrBothNull(parentListItemType,
							entryValuListItemType)) {
						result.add(entryValueField);
					}
				}
			}
		} else {
			List<IFieldInfo> itemFields = actualItemType.getFields();
			for (IFieldInfo field : itemFields) {
				ITypeInfo fieldType = field.getType();
				if (fieldType instanceof IListTypeInfo) {
					if (!isTabular()) {
						result.add(field);
					} else {
						ITypeInfo subListItemType = ((IListTypeInfo) fieldType)
								.getItemType();
						if (subListItemType instanceof IMapEntryTypeInfo) {
							IMapEntryTypeInfo entryType = (IMapEntryTypeInfo) subListItemType;
							ITypeInfo entryValueType = entryType
									.getValueField().getType();
							if (entryValueType instanceof IListTypeInfo) {
								ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType)
										.getItemType();
								if (ReflectionUIUtils.equalsOrBothNull(
										itemType, entryValuListItemType)) {
									result.add(field);
								}
							}
						} else {
							if (ReflectionUIUtils.equalsOrBothNull(itemType,
									subListItemType)) {
								result.add(field);
							}
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public IInfoCollectionSettings getItemInfoSettings(
			final IItemPosition itemPosition) {
		return new IInfoCollectionSettings() {
			
			@Override
			public boolean excludeMethod(IMethodInfo method) {
				return false;
			}
			
			@Override
			public boolean excludeField(IFieldInfo field) {
				Object item = itemPosition.getItem();
				if (item instanceof VirtualItem) {
					return ((IListTypeInfo) new MultiSubListField(reflectionUI,
							Collections.<IFieldInfo> emptyList()).getType())
							.getStructuralInfo().getItemInfoSettings(itemPosition).excludeField(field);
				}
				return getItemSubListCandidateFields(itemPosition).contains(field);
			}
			
			@Override
			public boolean allReadOnly() {
				return false;
			}
		};
	}

}