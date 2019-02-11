/*
*************************************************************************************
* Copyright 2011 Normation SAS
*************************************************************************************
*
* This file is part of Rudder.
*
* Rudder is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* In accordance with the terms of section 7 (7. Additional Terms.) of
* the GNU General Public License version 3, the copyright holders add
* the following Additional permissions:
* Notwithstanding to the terms of section 5 (5. Conveying Modified Source
* Versions) and 6 (6. Conveying Non-Source Forms.) of the GNU General
* Public License version 3, when you create a Related Module, this
* Related Module is not considered as a part of the work and may be
* distributed under the license agreement of your choice.
* A "Related Module" means a set of sources files including their
* documentation that, without modification of the Source Code, enables
* supplementary functions or services in addition to those offered by
* the Software.
*
* Rudder is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Rudder.  If not, see <http://www.gnu.org/licenses/>.

*
*************************************************************************************
*/

package com.normation.rudder.domain.eventlog

import com.normation.eventlog._
import org.joda.time.DateTime
import com.normation.inventory.domain.NodeId
import com.normation.utils.HashcodeCaching
import com.normation.rudder.domain.nodes.NodeInfo
import com.normation.rudder.domain.Constants

/**
 * Accept/refuse new server
 */

final case class InventoryLogDetails(
    nodeId : NodeId
  , inventoryVersion: DateTime
  , hostname        : String
  , fullOsName      : String
  , actorIp         : String
) extends HashcodeCaching

sealed trait AssetEventLog extends EventLog { override final val eventLogCategory = AssetLogCategory }

sealed trait InventoryEventLog extends AssetEventLog

object InventoryEventLog {

  /**
   * Print to XML an inventory details, used
   * for "accept" and "refuse" actions.
   */
  def toXml(
      logDetails: InventoryLogDetails
    , action    : String
  ) = {
    scala.xml.Utility.trim(
      <node action={action} fileFormat={Constants.XML_CURRENT_FILE_FORMAT.toString}>
        <id>{logDetails.nodeId.value}</id>
        <inventoryVersion>{logDetails.inventoryVersion}</inventoryVersion>
        <hostname>{logDetails.hostname}</hostname>
        <fullOsName>{logDetails.fullOsName}</fullOsName>
        <actorIp>{logDetails.actorIp}</actorIp>
      </node>
    )
  }
}

final case class AcceptNodeEventLog (
    override val eventDetails : EventLogDetails
) extends InventoryEventLog with HashcodeCaching {

  override val eventType = AcceptNodeEventLog.eventType
}

object AcceptNodeEventLog extends EventLogFilter {
  override val eventType = AcceptNodeEventType

  override def apply(x : (EventLogType, EventLogDetails)) : AcceptNodeEventLog = AcceptNodeEventLog(x._2)

  def fromInventoryLogDetails(
      id               : Option[Int] = None
    , principal        : EventActor
    , inventoryDetails : InventoryLogDetails
    , creationDate     : DateTime = DateTime.now()
    , severity         : Int = 100
    , description      : Option[String] = None
  ) : AcceptNodeEventLog = {
    val details = EventLog.withContent(InventoryEventLog.toXml(
      inventoryDetails, "accept"
    ) )

    AcceptNodeEventLog(EventLogDetails(id,None,principal,creationDate, None, severity, description, details))
  }
}

final case class RefuseNodeEventLog (
    override val eventDetails : EventLogDetails
) extends InventoryEventLog with HashcodeCaching {
  override val eventType = RefuseNodeEventLog.eventType
}

object RefuseNodeEventLog extends EventLogFilter {

  override val eventType = RefuseNodeEventType

  override def apply(x : (EventLogType, EventLogDetails)) : RefuseNodeEventLog = RefuseNodeEventLog(x._2)

  def fromInventoryLogDetails(
      id               : Option[Int] = None
    , principal        : EventActor
    , inventoryDetails : InventoryLogDetails
    , creationDate     : DateTime = DateTime.now()
    , severity         : Int = 100
    , description      : Option[String] = None
  ) : RefuseNodeEventLog = {
    val details = EventLog.withContent(InventoryEventLog.toXml(
      inventoryDetails, "refuse"
    ) )

    RefuseNodeEventLog(EventLogDetails(id,None,principal,creationDate, None, severity, description, details))
  }
}

// Accepted node part

final case class NodeLogDetails(
    node: NodeInfo
) extends HashcodeCaching

final case class DeleteNodeEventLog (
    override val eventDetails : EventLogDetails
) extends InventoryEventLog with HashcodeCaching {
  override val eventType = DeleteNodeEventLog.eventType

}

object DeleteNodeEventLog extends EventLogFilter {
  override val eventType = DeleteNodeEventType

  override def apply(x : (EventLogType, EventLogDetails)) : DeleteNodeEventLog = DeleteNodeEventLog(x._2)

  def fromInventoryLogDetails(
      id               : Option[Int] = None
    , principal        : EventActor
    , inventoryDetails : InventoryLogDetails
    , creationDate     : DateTime = DateTime.now()
    , severity         : Int = 100
    , description      : Option[String] = None
  ) : DeleteNodeEventLog = {
    val details = EventLog.withContent(InventoryEventLog.toXml(
      inventoryDetails, "delete"
    ) )

    DeleteNodeEventLog(EventLogDetails(id,None,principal,creationDate, None, severity, description, details))
  }
}

object AssetsEventLogsFilter {
  final val eventList : List[EventLogFilter] = List(
      AcceptNodeEventLog
    , RefuseNodeEventLog
    , DeleteNodeEventLog
    )
}