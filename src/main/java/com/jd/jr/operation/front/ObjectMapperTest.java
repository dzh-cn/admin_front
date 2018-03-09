package com.jd.jr.operation.front;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * test
 *
 * @author:dongzhihua
 * @time:2018/3/8 18:35:45
 */
public class ObjectMapperTest {
	public static void main(String[] args) throws ParseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Album album = new Album("Kind Of Blue");
		album.setLinks(new String[]{"link1", "link2"});
		List<String> songs = new ArrayList<String>();
		songs.add("So What");
		songs.add("Flamenco Sketches");
		songs.add("Freddie Freeloader");
		album.setSongs(songs);
		Artist artist = new Artist();
		artist.name = "Miles Davis";
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		artist.birthDate = format.parse("26-05-1926");
		album.setArtist(artist);
		album.addMusician("Miles Davis", "Trumpet, Band leader");
		album.addMusician("Julian Adderley", "Alto Saxophone");
		album.addMusician("Paul Chambers", "double bass");


		//让Json可以缩进，可读性更好，一般用在测试和开发阶段。
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		//让map的key按自然顺序排列
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		//日期输出格式
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
		mapper.setDateFormat(outputFormat);
		//默认情况下java类属性名就是输出的json字段名，但是可以采用注解的方式修改。如果你不想让java类和json绑定
		// 或者无法修改java类，这里采用如下另外一种方式。你需要重写如下两个方法，这个取决属性是public或者getter是public
		mapper.setPropertyNamingStrategy(new

												 PropertyNamingStrategy() {
													 /**
													  * @param config  Configuration in used: either SerializationConfig or DeserializationConfig, depending on whether method is called during serialization or deserialization
													  * @param field Field used to access property
													  * @param defaultName Default name that would be used for property in absence of custom strategy
													  * @return
													  */
													 @Override
													 public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
														 if (field.getFullName().equals("Artist#name"))
															 return "Artist-Name";
														 return super.nameForField(config, field, defaultName);
													 }


													 /**
													  * @param config  Configuration in used: either SerializationConfig or DeserializationConfig, depending on whether method is called during serialization or deserialization
													  * @param method  Method used to access property.
													  * @param defaultName Default name that would be used for property in absence of custom strategy
													  * @return
													  */
													 @Override
													 public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
														 if (method.getAnnotated().getDeclaringClass().equals(Album.class) && defaultName.equals("title"))
															 return "Album-Title";
														 return super.nameForGetterMethod(config, method, defaultName);
													 }
												 });
		//如果属性没有值，那么Json是会处理的，int类型为0，String类型为null，数组为[]，设置这个特性可以忽略空值属性
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		mapper = new ObjectMapper();
		Map m = new HashMap();
		m.put(1,123);
		System.out.println(mapper.writeValueAsString(m));
	}

	static class Album {
		public Album(String title) {
			this.title = title;
		}
		public void addMusician(String s1, String s2) {
			musicians.put(s1, s2);
		}

		private String title;
		private String[] links;
		private List<String> songs = new ArrayList<String>();
		private Artist artist;
		private Map<String, String> musicians = new HashMap<String, String>();

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String[] getLinks() {
			return links;
		}

		public void setLinks(String[] links) {
			this.links = links;
		}

		public List<String> getSongs() {
			return songs;
		}

		public void setSongs(List<String> songs) {
			this.songs = songs;
		}

		public Artist getArtist() {
			return artist;
		}

		public void setArtist(Artist artist) {
			this.artist = artist;
		}

		public Map<String, String> getMusicians() {
			return musicians;
		}

		public void setMusicians(Map<String, String> musicians) {
			this.musicians = musicians;
		}
	}


	static class Artist {
		public String name;
		public Date birthDate;
		public int age;
		public String homeTown;
		public List<String> awardsWon = new ArrayList<String>();
	}
}
