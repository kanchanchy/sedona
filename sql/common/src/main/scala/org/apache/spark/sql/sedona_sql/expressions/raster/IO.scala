/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.spark.sql.sedona_sql.expressions.raster

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenFallback
import org.apache.spark.sql.catalyst.expressions.{Expression, ImplicitCastInputTypes, UnsafeArrayData}
import org.apache.spark.sql.catalyst.util.GenericArrayData
import org.apache.spark.sql.sedona_sql.expressions.UserDataGeneratator
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO

case class RS_Array(inputExpressions: Seq[Expression])
  extends Expression with ImplicitCastInputTypes with CodegenFallback with UserDataGeneratator {
  override def nullable: Boolean = false

  override def eval(inputRow: InternalRow): Any = {
    // This is an expression which takes one input expressions
    assert(inputExpressions.length == 2)
    val len =inputExpressions(0).eval(inputRow).asInstanceOf[Int]
    val num = inputExpressions(1).eval(inputRow).asInstanceOf[Double]
    val result = createarray(len, num)
    new GenericArrayData(result)
  }

  // Generate an empty band for the given spectral band in ageotiff image
  private def createarray(len:Int, num:Double):Array[Double] = {

    val result = new Array[Double](len)
    for(i<-0 until len) {
      result(i) = num
    }
    result
  }

  override def inputTypes: Seq[AbstractDataType] = Seq(IntegerType, DoubleType)

  override def dataType: DataType = ArrayType(DoubleType)

  override def children: Seq[Expression] = inputExpressions

  protected def withNewChildrenInternal(newChildren: IndexedSeq[Expression]) = {
    copy(inputExpressions = newChildren)
  }
}

case class RS_Base64(inputExpressions: Seq[Expression])
  extends Expression with CodegenFallback with UserDataGeneratator {
  override def nullable: Boolean = false

  override def eval(inputRow: InternalRow): Any = {
    // This is an expression which takes one input expressions
    assert(inputExpressions.length>=5 && inputExpressions.length<=6)

    val height = inputExpressions(0).eval(inputRow).asInstanceOf[Int]
    val width = inputExpressions(1).eval(inputRow).asInstanceOf[Int]
    val band1 = inputExpressions(2).eval(inputRow).asInstanceOf[GenericArrayData].toDoubleArray()
    val band2 = inputExpressions(3).eval(inputRow).asInstanceOf[GenericArrayData].toDoubleArray()
    val band3 = inputExpressions(4).eval(inputRow).asInstanceOf[GenericArrayData].toDoubleArray()
    var bufferedimage:BufferedImage = null
    if(inputExpressions.length==5) {
        bufferedimage = getBufferedimage(band1, band2, band3, null , height, width)
      }
    else {
      var band4:Array[Double] = null
      if(inputExpressions(5).eval(inputRow).getClass.toString() == "class org.apache.spark.sql.catalyst.expressions.UnsafeArrayData") {
        band4 = inputExpressions(5).eval(inputRow).asInstanceOf[UnsafeArrayData].toDoubleArray()
      }
      else {
        band4 = inputExpressions(5).eval(inputRow).asInstanceOf[GenericArrayData].toDoubleArray()
      }
      bufferedimage = getBufferedimage(band1, band2, band3, band4, height, width)
    }

    val result = convertToBase64(bufferedimage)
    UTF8String.fromString(result)
  }

  private def getBufferedimage(band1:Array[Double], band2:Array[Double], band3:Array[Double], band4:Array[Double], height:Int, width:Int): BufferedImage = {
    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    var w = 0
    var h = 0
    band4 match {
      case null => {
        for (i <- 0 until (height * width)) {
          if(i>0 && i%height==0) {
            h+=1
          }
          w = i%width
          image.setRGB(w, h, new Color(band1(i).toInt, band2(i).toInt, band3(i).toInt, 255).getRGB())
        }
        image
      }
      case _ => {
        for (i <- 0 until (height * width)) {
          if(i>0 && i%height==0) {
            h+=1
          }
          w = i%width
          image.setRGB(w, h, new Color(band1(i).toInt, band2(i).toInt, band3(i).toInt, band4(i).toInt).getRGB())

        }
        image
      }

    }
  }

  // Convert Buffered image to Base64 String
   private def convertToBase64(image: BufferedImage): String = {

    val os = new ByteArrayOutputStream()
    ImageIO.write(image,"png", os)
    Base64.getEncoder.encodeToString(os.toByteArray)
  }


  override def dataType: DataType = StringType

  override def children: Seq[Expression] = inputExpressions

  protected def withNewChildrenInternal(newChildren: IndexedSeq[Expression]) = {
    copy(inputExpressions = newChildren)
  }
}

case class RS_HTML(inputExpressions: Seq[Expression])
  extends Expression with CodegenFallback with UserDataGeneratator {
  override def nullable: Boolean = false

  override def eval(inputRow: InternalRow): Any = {
    // This is an expression which takes one input expressions
    val encodedstring =inputExpressions(0).eval(inputRow).asInstanceOf[UTF8String].toString
    // Add image width if needed
    var imageWidth = "200"
    if (inputExpressions.length == 2) imageWidth = inputExpressions(1).eval(inputRow).asInstanceOf[UTF8String].toString
    val result = htmlstring(encodedstring, imageWidth)
    UTF8String.fromString(result)
  }

  // create HTML string from Base64 string
  private def htmlstring(encodestring: String, imageWidth: String): String = {
    "<img src=\"" + createmainstring(encodestring) + "\" width=\"" + imageWidth + "\" />"
  }

  private def createmainstring(encodestring:String): String = {

    val result = s"data:image/png;base64,$encodestring"
    result
  }
  override def dataType: DataType = StringType

  override def children: Seq[Expression] = inputExpressions

  protected def withNewChildrenInternal(newChildren: IndexedSeq[Expression]) = {
    copy(inputExpressions = newChildren)
  }
}

