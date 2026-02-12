package com.example.poc.infrastructure.mapping;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ObjectFactory;
import com.example.poc.domain.Product;
import com.example.poc.domain.vo.Money;
import com.example.poc.domain.vo.ProductRatings;
import com.example.poc.domain.vo.Stock;
import com.example.poc.web.ProductCreateDto;
import com.example.poc.web.ProductDto;
import com.example.poc.web.ProductRatingsDto;
import com.example.poc.web.StockDto;
@Mapper(componentModel = "spring")
public abstract class ProductMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    @Mapping(source = "id", target = "id")
    @Mapping(source = "sku", target = "sku")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "price.amount", target = "price")
    @Mapping(source = "price.currency", target = "currency")
    @Mapping(target = "stock", expression = "java(toStockDto(domain.getStock()))")
    @Mapping(target = "ratings", expression = "java(toProductRatingsDto(domain.getRatings()))")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "formatDateTime")
    public abstract ProductDto toDto(Product domain);
    public Product toDomain(ProductCreateDto dto) {
        return createProduct(dto);
    }

    protected Money toMoney(String price, String currency) {
        if (price == null) {
            // Retornar um Money com valor zero e moeda padrão para evitar NPE
            return new Money(BigDecimal.ZERO, Money.DEFAULT_CURRENCY);
        }
        String currencyOrDefault = currency == null || currency.isBlank() ? Money.DEFAULT_CURRENCY : currency;
        return new Money(new BigDecimal(price), currencyOrDefault);
    }

    protected StockDto toStockDto(Stock stock) {
        if (stock == null) {
            return null;
        }
        return new StockDto(stock.available(), stock.reserved(), stock.total());
    }

    protected ProductRatingsDto toProductRatingsDto(ProductRatings ratings) {
        if (ratings == null) {
            return null;
        }
        return new ProductRatingsDto(ratings.average(), ratings.count());
    }

    @Named("formatDateTime")
    protected String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(FORMATTER);
    }

    @ObjectFactory
    protected Product createProduct(ProductCreateDto dto) {
        Product product = Product.create(dto.sku(), dto.name(), dto.description(), dto.category(), toMoney(dto.price(), dto.currency()), dto.initialStock(), dto.images());
        if (dto.specifications() != null) {
            product.updateSpecifications(dto.specifications());
        }
        return product;
    }
}
