package se.crimzone.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-06-15T17:27:33.477Z")
public class GeoJsonPoint {

	private String type = null;
	private List<Double> coordinates = new ArrayList<Double>();

	/**
	 **/
	public GeoJsonPoint type(String type) {
		this.type = type;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * order: [longitude, latitude]
	 **/
	public GeoJsonPoint coordinates(List<Double> coordinates) {
		this.coordinates = coordinates;
		return this;
	}


	@ApiModelProperty(required = true, value = "order: [longitude, latitude]")
	@JsonProperty("coordinates")
	public List<Double> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<Double> coordinates) {
		this.coordinates = coordinates;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GeoJsonPoint geoJsonPoint = (GeoJsonPoint) o;
		return Objects.equals(type, geoJsonPoint.type) &&
				Objects.equals(coordinates, geoJsonPoint.coordinates);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, coordinates);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GeoJsonPoint {\n");

		sb.append("    type: ").append(toIndentedString(type)).append("\n");
		sb.append("    coordinates: ").append(toIndentedString(coordinates)).append("\n");
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

