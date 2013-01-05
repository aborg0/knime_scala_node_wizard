/**
 *
 */
package org.knime.workbench.scala

import org.knime.core.data.`def`.IntCell
import org.knime.core.data.`def`.LongCell
import org.knime.core.data.`def`.DoubleCell
import org.knime.core.data.`def`.BooleanCell
import org.knime.core.data.`def`.StringCell
import org.knime.core.data.date.DateAndTimeCell
import org.knime.core.data.IntValue
import org.knime.core.data.LongValue
import org.knime.core.data.DoubleValue
import org.knime.core.data.BooleanValue
import org.knime.core.data.StringValue
import org.knime.core.data.date.DateAndTimeValue
import org.knime.core.data.DataColumnSpecCreator
import org.knime.core.data.DataColumnSpec
import org.knime.core.data.DataTableSpec
import org.knime.core.data.DataCell
import org.knime.core.node.defaultnodesettings.SettingsModelInteger
import org.knime.core.node.defaultnodesettings.SettingsModelDouble
import org.knime.core.node.defaultnodesettings.SettingsModelLong
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean
import org.knime.core.node.defaultnodesettings.SettingsModelString
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray
import org.knime.core.node.ExecutionContext
import org.knime.core.data.RowKey
import org.knime.core.data.collection.CollectionCellFactory
import scala.collection.convert.wrapAsJava.asJavaCollection
import org.knime.core.data.collection.SetDataValue
import org.knime.core.data.collection.ListDataValue
import org.knime.core.node.BufferedDataContainer
import scala.language.implicitConversions
import org.knime.core.data.`def`.DefaultRow
import org.knime.core.data.DataRow

/**
 * Implicits to help make working with KNIME easier to read.
 *
 * @author Gabor Bakos
 */
object Implicits {
  implicit def intToIntCell(v: Int): IntValue = new IntCell(v)
  implicit def longToLongCell(v: Long): LongValue = new LongCell(v)
  implicit def doubleToDoubleCell(v: Double): DoubleValue = new DoubleCell(v)
  implicit def booleanToBooleanCell(v: Boolean): BooleanValue = BooleanCell.get(v)
  implicit def stringToStringCell(v: String): StringValue = new StringCell(v)
  implicit def stringToRowKey(key: String): RowKey = new RowKey(key)

  implicit def setToSetDataValue(set: collection.Set[DataCell]): SetDataValue = CollectionCellFactory.createSetCell(asJavaCollection(set))
  implicit def setToSetDataValue(set: collection.Seq[DataCell]): ListDataValue = CollectionCellFactory.createListCell(asJavaCollection(set))

  implicit def intToDataCell(v: Int): DataCell = new IntCell(v)
  implicit def longToDataCell(v: Long): DataCell = new LongCell(v)
  implicit def doubleToDataCell(v: Double): DataCell = new DoubleCell(v)
  implicit def booleanToDataCell(v: Boolean): DataCell = BooleanCell.get(v)
  implicit def stringToDataCell(v: String): DataCell = new StringCell(v)

  implicit def setToSetDataCell(set: collection.Set[DataCell]): DataCell = CollectionCellFactory.createSetCell(asJavaCollection(set))
  //Maybe this is a bad idea
  implicit def setToSetDataCell(set: collection.Seq[DataCell]): DataCell = CollectionCellFactory.createListCell(asJavaCollection(set))

  //Maybe this is a bad idea, as it can be misunderstood.
  implicit def longToDateAndTimeCell(v: Long): DateAndTimeValue = new DateAndTimeCell(v, true, true, true)

  implicit def intValueToInt(v: IntValue) = v.getIntValue
  implicit def longValueToLong(v: LongValue) = v.getLongValue
  implicit def doubleValueToDouble(v: DoubleValue) = v.getDoubleValue
  implicit def booleanValueToBoolean(v: BooleanValue) = v.getBooleanValue
  implicit def stringValueToString(v: StringValue) = v.getStringValue
  implicit def dateAndTimeValueToCalendar(v: DateAndTimeValue) = v.getUTCCalendarClone
  implicit def rowKeyToString(key: RowKey) = key.getString
  
  trait WithData {
    def withData(data: Seq[DataCell]): DataRow
  }
  
  implicit def rowKeyWithData(key: RowKey): WithData = new WithData {
    def withData(data: Seq[DataCell]): DataRow = new DefaultRow(key, data: _*)
  }

  implicit def settingsModelIntegerToInt(sm: SettingsModelInteger) = sm.getIntValue
  implicit def settingsModelLongToLong(sm: SettingsModelLong) = sm.getLongValue
  implicit def settingsModelDoubleToDouble(sm: SettingsModelDouble) = sm.getDoubleValue
  implicit def settingsModelBooleanToBoolean(sm: SettingsModelBoolean) = sm.getBooleanValue
  implicit def settingsModelStringToString(sm: SettingsModelString) = sm.getStringValue
  implicit def settingsModelStringArrayToStringSeq(sm: SettingsModelStringArray): collection.Seq[String] = sm.getStringArrayValue.toSeq

  implicit def createDataContainer(tableSpec: DataTableSpec)(implicit exec: ExecutionContext) = exec.createDataContainer(tableSpec)

  implicit def dataColumnSpecCreatorToDataColumnSpec(creator: DataColumnSpecCreator) = creator.createSpec

  implicit def dataColumnSpecsToDataTableSpec(colSpecs: Seq[DataColumnSpec]) = new DataTableSpec(colSpecs: _*)
  implicit def dataColumnSpecsToDataTableSpec(colSpecs: Array[DataColumnSpec]) = new DataTableSpec(colSpecs: _*)
  implicit def bufferedDataContainerToBufferedDataTable(container: BufferedDataContainer) = {
    container.close
    container.getTable
  }
  
  def checkCanceled(implicit exec: ExecutionContext) = exec.checkCanceled
  def checkCancelled(implicit exec: ExecutionContext) = exec.checkCanceled
  def setProgress(progress: Double, message: String)(implicit exec: ExecutionContext) = exec.setProgress(progress, message)
  def setProgress(message: String)(implicit exec: ExecutionContext) = exec.setProgress(message)
  def setProgress(progress: Double)(implicit exec: ExecutionContext) = exec.setProgress(progress)
}