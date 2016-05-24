package se.crimzone.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.Objects;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaInflectorServerCodegen", date = "2016-05-24T19:10:53.647Z")
public class Crime {

	private Long id = null;
	private Double latitude = null;
	private Double longitude = null;
	private Date time = null;
	private String title = null;
	private String description = null;


	/**
	 **/
	public Crime id(Long id) {
		this.id = id;
		return this;
	}


	@ApiModelProperty(example = "null", required = true, value = "")
	@JsonProperty("id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	/**
	 **/
	public Crime latitude(Double latitude) {
		this.latitude = latitude;
		return this;
	}


	@ApiModelProperty(example = "null", required = true, value = "")
	@JsonProperty("latitude")
	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}


	/**
	 **/
	public Crime longitude(Double longitude) {
		this.longitude = longitude;
		return this;
	}


	@ApiModelProperty(example = "null", required = true, value = "")
	@JsonProperty("longitude")
	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}


	/**
	 **/
	public Crime time(Date time) {
		this.time = time;
		return this;
	}


	@ApiModelProperty(example = "null", required = true, value = "")
	@JsonProperty("time")
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}


	/**
	 **/
	public Crime title(String title) {
		this.title = title;
		return this;
	}


	@ApiModelProperty(example = "null", required = true, value = "")
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


	@ApiModelProperty(example = "null", required = true, value = "")
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
				Objects.equals(latitude, crime.latitude) &&
				Objects.equals(longitude, crime.longitude) &&
				Objects.equals(time, crime.time) &&
				Objects.equals(title, crime.title) &&
				Objects.equals(description, crime.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, latitude, longitude, time, title, description);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Crime {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
		sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
		sb.append("    time: ").append(toIndentedString(time)).append("\n");
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

