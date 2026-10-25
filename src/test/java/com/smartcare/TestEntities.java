   package com.smartcare;

   import com.smartcare.entity.BaseEntity;

   public final class TestEntities {

       private TestEntities() {}

       public static <T extends BaseEntity> T withId(T entity, Long id) {
           entity.setId(id);
           return entity;
       }
   }
