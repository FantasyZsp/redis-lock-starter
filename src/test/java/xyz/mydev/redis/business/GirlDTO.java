package xyz.mydev.redis.business;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * @author ZSP
 */
@Data
@ToString(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class GirlDTO {

    private Integer id;

    private String cupSize;

    private Integer age;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GirlDTO girlDTO = (GirlDTO) o;
        return Objects.equals(id, girlDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String hasId(String id) {
        return id + "mark";
    }
}
