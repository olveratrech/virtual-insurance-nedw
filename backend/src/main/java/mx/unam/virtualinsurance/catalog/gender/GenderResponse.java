package mx.unam.virtualinsurance.catalog.gender;

public record GenderResponse(
	Long id,
	String key,
	String description
) {

	public static GenderResponse from(Gender gender) {
		return new GenderResponse(
			gender.getGeneroId(),
			gender.getKey(),
			gender.getDescription()
		);
	}
}
