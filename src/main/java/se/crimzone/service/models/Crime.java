package se.crimzone.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-06-04T15:05:04.671Z")
public class Crime {

	private Integer id = null;
	private Integer time = null;
	private Float latitude = null;
	private Float longitude = null;
	private String title = null;
	private String description = null;

	/**
	 **/
	public Crime id(Integer id) {
		this.id = id;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("_id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 **/
	public Crime time(Integer time) {
		this.time = time;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("time")
	public Integer getTime() {
		return time;
	}

	public void setTime(Integer time) {
		this.time = time;
	}

	/**
	 **/
	public Crime latitude(Float latitude) {
		this.latitude = latitude;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("latitude")
	public Float getLatitude() {
		return latitude;
	}

	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	/**
	 **/
	public Crime longitude(Float longitude) {
		this.longitude = longitude;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("longitude")
	public Float getLongitude() {
		return longitude;
	}

	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	/**
	 **/
	public Crime title(String title) {
		this.title = title;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 **/
	public Crime description(String description) {
		this.description = description;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Crime crime = (Crime) o;
		return Objects.equals(id, crime.id) &&
				Objects.equals(time, crime.time) &&
				Objects.equals(latitude, crime.latitude) &&
				Objects.equals(longitude, crime.longitude) &&
				Objects.equals(title, crime.title) &&
				Objects.equals(description, crime.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, time, latitude, longitude, title, description);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Crime {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    time: ").append(toIndentedString(time)).append("\n");
		sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
		sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
		sb.append("    title: ").append(toIndentedString(title)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}

