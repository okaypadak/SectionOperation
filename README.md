# Java Spring ile Yüksek Performanslı Parçalı Batch İşleme Kütüphanesi

## Giriş
Geliştirdiğim bu Java Spring kütüphanesi, saveAll ve findByInIds metodlarını kullanarak parçalı batch işlemlerini optimize etmeyi amaçlamaktadır. Bu kütüphane, veritabanı işlemlerinde performans artışı sağlayarak, büyük veri setleri üzerinde etkili çalışma imkanı sunmaktadır.

## Temel Özellikler

### @Section Annotation
- Repository sınıflarına eklenen özel @Section annotation'ı sayesinde, parçalı batch işlemlerini kolayca belirleyip yönetmek mümkündür.
- İşlem bölümleri sayesinde karmaşık veri işlemleri daha okunabilir ve yönetilebilir hale gelir.

#### 1. saveAll Metodu
- Kütüphane, saveAll metodunu parçalı batch işlemlerinde kullanarak veritabanına büyük veri setlerini daha etkili bir şekilde kaydetmeyi hedefler.
- Otomatik olarak transaction yönetimi sağlayarak veri bütünlüğünü korur.

#### 2. findByInIds Metodu
- findByInIds metodunu kullanarak, parçalı batch işlemleriyle büyük veri kümesinden veri çekme süreçlerini optimize eder.
- Veritabanı sorgularını paralel olarak çalıştırarak performansı artırır.

## Kütüphanenin Avantajları

1. **Performans Artışı:**
   - Parçalı batch işlemleri, büyük veri setleriyle çalışan uygulamalarda belirgin bir performans artışı sağlar.

2. **Transaction Yönetimi:**
   - Otomatik transaction yönetimi ile veri bütünlüğünü korur, hatalı işlemleri önler.

3. **Okunabilirlik ve Yönetilebilirlik:**
   - @Section annotation'ı ile işlem bölümleri, kodun daha okunabilir ve yönetilebilir olmasını sağlar.

## Kullanım Örnekleri

```java
@Repository
@Section(1000)
public interface PersonRepository extends CrudRepository<PersonEntity, Long> {
}

