package org.tkit.onecx.tenant.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.tenant.bff.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.tenant.bff.rs.internal.model.RefTypeDTO;
import gen.org.tkit.onecx.tenant.image.client.model.ImageInfo;
import gen.org.tkit.onecx.tenant.image.client.model.RefType;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ImagesMapper {

    ImageInfoDTO map(ImageInfo image);

    ImageInfo map(ImageInfoDTO image);

    RefType map(RefTypeDTO refType);

}
