package xy.reflect.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.HiddenNullableFacetsInfoProxyGenerator;

public class TableTreeModelExample {

	public static void main(String[] args) {
		ReflectionUI reflectionUI = new ReflectionUI() {
			ReflectionUI thisReflectionUI = this;

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new HiddenNullableFacetsInfoProxyGenerator(thisReflectionUI) {

					@Override
					protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
						if (type.getName().equals(Product.class.getName())) {
							List<ITypeInfo> result = new ArrayList<ITypeInfo>();
							for (Class<?> c : Arrays.<Class<?>> asList(Book.class, CompactDisc.class, Shoes.class,
									Package.class, Loaning.class)) {
								result.add(getTypeInfo(new JavaTypeInfoSource(c)));
							}
							return result;
						}
						return super.getPolymorphicInstanceSubTypes(type);
					}

				}.get(super.getTypeInfo(typeSource));
			}

		};
		new SwingRenderer(reflectionUI).openObjectDialog(null, new Catalog(), false);

	}

	public static class Catalog {
		private Product[] products = new Product[0];

		public Product[] getProducts() {
			return products;
		}

		public void setProducts(Product[] products) {
			this.products = products;
		}

	}

	public static abstract class Product {
		private String name = "";
		private String description = "";
		private int price;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public int getPrice() {
			return price;
		}

		public void setPrice(int price) {
			this.price = price;
		}

	}

	public static class CompactDisc extends Product {
		private String arstist = "";
		private String genre = "";

		public String getArstist() {
			return arstist;
		}

		public void setArstist(String arstist) {
			this.arstist = arstist;
		}

		public String getGenre() {
			return genre;
		}

		public void setGenre(String genre) {
			this.genre = genre;
		}
	}

	public static class Book extends Product {
		private String author = "";

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}
	}

	public static class Shoes extends Product {
		private int size;
		private boolean female;

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public boolean isFemale() {
			return female;
		}

		public void setFemale(boolean female) {
			this.female = female;
		}
	}

	public static class Package extends Product {
		private List<Product> products = new ArrayList<TableTreeModelExample.Product>();

		public List<Product> getProducts() {
			return products;
		}

		public void setProducts(List<Product> products) {
			this.products = products;
		}

	}

	public static class Loaning extends Product {
		private Map<Integer, Product[]> productsByDuration = new HashMap<Integer, TableTreeModelExample.Product[]>();

		public Map<Integer, Product[]> getProductsByDuration() {
			return productsByDuration;
		}

		public void setProductsByDuration(Map<Integer, Product[]> productsByDuration) {
			this.productsByDuration = productsByDuration;
		}

	}

}
