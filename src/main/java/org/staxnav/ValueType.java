/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.staxnav;

/**
 * The type of a value.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ValueType<V>
{
   /**
    * Returns a value type for an enum class.
    *
    * @param enumClass the enum class
    * @param <E> the enum parameter type
    * @return the corresponding value type
    */
   public static <E extends Enum<E>> ValueType<E> get(Class<E> enumClass)
   {
      return new EnumType<E>(enumClass);
   }

   public static final ValueType<String> STRING = new ValueType<String>()
   {
      @Override
      protected String parse(String s) throws Exception
      {
         return s;
      }
   };

   public static final ValueType<String> TRIMMED_STRING = new ValueType<String>()
   {
      @Override
      protected String parse(String s) throws Exception
      {
         return s.trim();
      }
   };

   public static final ValueType<Boolean> BOOLEAN = new ValueType<Boolean>()
   {
      @Override
      protected Boolean parse(String s) throws Exception
      {
         return Boolean.parseBoolean(s.trim());
      }
   };

   public static final ValueType<Integer> INTEGER = new ValueType<Integer>()
   {
      @Override
      protected Integer parse(String s) throws Exception
      {
         return Integer.parseInt(s.trim());
      }
   };

   protected static class EnumType<E extends Enum<E>> extends ValueType<E>
   {

      /** . */
      private final Class<E> enumType;

      public EnumType(Class<E> enumType)
      {
         this.enumType = enumType;
      }

      @Override
      protected E parse(String s) throws Exception
      {
         return Enum.valueOf(enumType, s.trim());
      }
   }

   protected ValueType()
   {
   }

   /**
    * Parse the string to the java type for a non null parameter value.
    *
    * @param s the string value to parse
    * @return the parsed value
    * @throws Exception any exception that would prevent the type conversion to happen
    */
   protected abstract V parse(String s) throws Exception;

   /**
    * Parse the string to the java type for a specifed value.
    *
    * @param s the string value to parse
    * @return the parsed value
    * @throws NullPointerException if the provided value is null
    * @throws TypeConversionException any exception that would prevent the type conversion to happen
    */
   public final V convert(String s) throws NullPointerException, TypeConversionException
   {
      if (s == null)
      {
         throw new NullPointerException();
      }
      try
      {
         return parse(s);
      }
      catch (Exception e)
      {
         if (e instanceof TypeConversionException)
         {
            throw (TypeConversionException)e;
         }
         else
         {
            throw new TypeConversionException(e, "Could not parse string value " + s);
         }
      }
   }
}
