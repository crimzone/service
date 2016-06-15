package se.crimzone.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.Objects;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-06-15T17:27:33.477Z")
public class Crime {

	private Integer id = null;
	private Date time = null;
	private GeoJsonPoint location = null;
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
	public Crime time(Date time) {
		this.time = time;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("time")
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 **/
	public Crime location(GeoJsonPoint location) {
		this.location = location;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("location")
	public GeoJsonPoint getLocation() {
		return location;
	}

	public void setLocation(GeoJsonPoint location) {
		this.location = location;
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
				Objects.equals(location, crime.location) &&
				Objects.equals(title, crime.title) &&
				Objects.equals(description, crime.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, time, location, title, description);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Crime {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    time: ").append(toIndentedString(time)).append("\n");
		sb.append("    location: ").append(toIndentedString(location)).append("\n");
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

