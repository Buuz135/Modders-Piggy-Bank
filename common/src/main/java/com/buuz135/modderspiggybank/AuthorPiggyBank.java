package com.buuz135.modderspiggybank;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AuthorPiggyBank(String author, int primary_color, int secondary_color, List<String> alternate, List<Link> links) {
	public static MapCodec<AuthorPiggyBank> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.fieldOf("author").forGetter(AuthorPiggyBank::author),
			Codec.STRING.optionalFieldOf("primary_color", "0x55FFFF").xmap(Integer::decode, String::valueOf).forGetter(AuthorPiggyBank::primary_color),
			Codec.STRING.optionalFieldOf("secondary_color", "0x55FFFF").xmap(Integer::decode, String::valueOf).forGetter(AuthorPiggyBank::secondary_color),
			Codec.STRING.listOf().optionalFieldOf("alternate_attribution", new ArrayList<>()).forGetter(AuthorPiggyBank::alternate),
			Link.CODEC.codec().listOf().fieldOf("links").forGetter(AuthorPiggyBank::links)
	).apply(instance, AuthorPiggyBank::new));

    public record Link (String type, String url){
		public static MapCodec<Link> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.STRING.fieldOf("type").forGetter(Link::type),
				Codec.STRING.fieldOf("url").forGetter(Link::url)
		).apply(instance, Link::new));
    }
}
