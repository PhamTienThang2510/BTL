# Luong tao mockup tu san pham den anh render

Tai lieu nay mo ta luong hoat dong tu luc nguoi dung an vao mot san pham cho den khi render xong mockup va hien thi anh tren man hinh.

## 1. Mo chi tiet san pham

- Vi tri code:
  - `app/src/main/java/com/example/btl/Adapters/ProductAdapter.kt`
- Dau vao:
  - Su kien click vao item san pham.
  - `Product` hien tai (co `product_id`, `name`, `description`).
- Xu ly:
  - Tao `Bundle` va navigate sang detail.
- Dau ra:
  - Man chi tiet san pham duoc mo, co args `product_id`, `product_name`, `product_description`.

```kotlin
// ProductAdapter.kt (trong onBind, khi click item)
val args = Bundle().apply {
    putInt("product_id", product.product_id)
    putString("product_name", product.name)
    putString("product_description", product.description)
}
navHostFragment?.navController?.navigate(R.id.action_home_to_detail_product, args)
```

## 2. Vao man hinh mockup

- Vi tri code:
  - `app/src/main/java/com/example/btl/Fragments/Shopping/ProductMockupFragment.kt`
- Dau vao:
  - Args tu detail (it nhat co `variant_id`, `product_id`).
- Xu ly:
  - Doc args va hien template name.
  - Khi nhan nut tao mockup thi goi `uploadAndRenderMockup()`.
- Dau ra:
  - Bat dau luong render mockup.

```kotlin
// ProductMockupFragment.kt
selectedVariantId = getPositiveIntArg(arguments, "variant_id", 0)
selectedProductId = getPositiveIntArg(arguments, "product_id", 0)

binding.buttonCreateMockup.setOnClickListener {
    uploadAndRenderMockup()
}
```

## 3. Lay template theo variant

- Vi tri code:
  - `app/src/main/java/com/example/btl/Repository/MockupRepository.kt`
- Dau vao:
  - `variantId` (int), `active=true`.
- Xu ly:
  - Goi API `GET /mockups/templates`.
  - Parse list template, chon template active.
- Dau ra:
  - `TemplateCandidate` (chua `templateId`, `isActive`, `smartObjects`).

```kotlin
// MockupRepository.kt
val response = mockupApi.getTemplatesFixed(
    variantId = variantId,
    active = true,
    token = authHeader
)
val templates = parseTemplatesResponse(response.body()!!)
val activeTemplate = templates.firstOrNull { it.isActive } ?: templates.first()
```

## 4. Tao render mockup

- Vi tri code:
  - `app/src/main/java/com/example/btl/Repository/MockupRepository.kt`
- Dau vao:
  - `template_id`, `design_image_url`, `product_id` (optional), `render_config` (optional).
- Xu ly:
  - Tao `CreateMockupRenderDto`.
  - Goi API `POST /mockups/render`.
- Dau ra:
  - Response JSON chua `render_id`, `status`, `rendered_image_url`.

```kotlin
// MockupRepository.kt
val renderConfig = parseRenderConfig(template.smartObjects)
val request = CreateMockupRenderDto(
    template_id = template.templateId,
    design_image_url = designImageUrl,
    product_id = productId,
    render_config = renderConfig
)
val response = mockupApi.createRenderFixed(request, authHeader)
```

## 5. Nhan response va hien thi anh

- Vi tri code:
  - `app/src/main/java/com/example/btl/Fragments/Shopping/ProductMockupFragment.kt`
- Dau vao:
  - `MockupRenderResponseDto` (co `rendered_image_url`).
- Xu ly:
  - Neu la duong dan tuong doi thi noi base URL.
  - Glide load vao `ivProductPreview`.
- Dau ra:
  - Hien anh mockup tren UI.

```kotlin
// ProductMockupFragment.kt
val renderedImageUrl = mockupResponse.rendered_image_url
val fullImageUrl = when {
    renderedImageUrl.startsWith("http") -> renderedImageUrl
    renderedImageUrl.startsWith("/uploads/") -> "http://10.0.2.2:3000$renderedImageUrl"
    else -> "http://10.0.2.2:3000/uploads/$renderedImageUrl"
}
Glide.with(this).load(fullImageUrl).into(binding.ivProductPreview)
```

## 6. Trang thai va thong bao

- Vi tri code:
  - `app/src/main/java/com/example/btl/Fragments/Shopping/ProductMockupFragment.kt`
- Dau vao:
  - Ket qua render thanh cong/that bai.
- Xu ly:
  - Bat/tat progress.
  - Toast thong bao.
- Dau ra:
  - UI phan hoi ro rang cho nguoi dung.

## Ghi chu ky thuat

- Endpoint render duoc dinh nghia trong `docs/api-architecture.md`:
  - `POST /mockups/render` nhan `template_id`, `design_image_url`, `product_id`, `render_config`.
- `render_config` duoc suy ra tu `smart_objects` cua template (neu backend cung cap).
- Neu server yeu cau auth cho render, can cap token thong qua header Authorization.
